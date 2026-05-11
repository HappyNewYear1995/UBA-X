package com.huanniankj.module.storage.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.huanniankj.framework.datasource.core.enums.DataSourceEnum;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.huanniankj.module.storage.dal.dataobject.UserBehaviorLogDO;
import com.huanniankj.module.storage.dal.clickhouse.UserBehaviorLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@DS(DataSourceEnum.CLICKHOUSE)
public class EventStorageServiceImpl implements EventStorageService {

    @Resource
    private UserBehaviorLogMapper userBehaviorLogMapper;

    @Override
    public void saveEventsBatch(List<String> eventJsonList) {
        if (eventJsonList == null || eventJsonList.isEmpty()) {
            return;
        }

        // TODO: 当前为单条循环 insert 操作，为了发挥 ClickHouse 的最大写入性能，后续应改为真正的 JDBC 批量插入 (Batch Insert)

        List<UserBehaviorLogDO> events = new ArrayList<>(eventJsonList.size());
        for (String json : eventJsonList) {
            try {
                // 1. 将 JSON 转为 JSONObject (方便处理嵌套和特殊字段)
                JSONObject jsonObj = JSONUtil.parseObj(json);
                
                // 2. 将 JSONObject 映射为 MyBatis-Plus 的 DO 实体
                UserBehaviorLogDO logDO = JSONUtil.toBean(jsonObj, UserBehaviorLogDO.class);
                
                // 3. 处理时间戳转换为 LocalDateTime (DO 中的时间是 LocalDateTime 类型)
                Long startTimeTs = jsonObj.getLong("startTime");
                if (startTimeTs != null) {
                    // 使用 Instant 和 ZoneId 转换，确保毫秒精度绝对不丢失
                    logDO.setStartTime(java.time.LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(startTimeTs), java.time.ZoneId.systemDefault()));
                }
                
                Long endTimeTs = jsonObj.getLong("endTime");
                if (endTimeTs != null) {
                    logDO.setEndTime(java.time.LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(endTimeTs), java.time.ZoneId.systemDefault()));
                }

                // 4. 保存原始报文 (如果后续排查或重算数据需要，这是一个兜底保障)
                logDO.setRawData(json);

                events.add(logDO);
            } catch (Exception e) {
                log.error("解析通用日志 JSON 失败: {}", json, e);
            }
        }

        if (events.isEmpty()) {
            return;
        }

        long start = System.currentTimeMillis();
        // 5. 调用 MyBatis-Plus 提供的批量插入方法
        for (UserBehaviorLogDO event : events) {
            userBehaviorLogMapper.insert(event);
        }

        log.info("【存储模块】大宽表批量写入 ClickHouse 成功, 条数: {}, 耗时: {} ms", events.size(), (System.currentTimeMillis() - start));
    }
}
