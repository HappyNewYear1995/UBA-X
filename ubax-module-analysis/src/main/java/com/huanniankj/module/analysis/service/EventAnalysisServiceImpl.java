package com.huanniankj.module.analysis.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.huanniankj.module.analysis.controller.vo.EventAnalysisReqVO;
import com.huanniankj.module.analysis.controller.vo.EventAnalysisRespVO;
import com.huanniankj.module.analysis.dal.dataobject.EventAnalysisDO;
import com.huanniankj.module.analysis.dal.clickhouse.EventAnalysisMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EventAnalysisServiceImpl implements EventAnalysisService {

    @Resource
    private EventAnalysisMapper eventAnalysisMapper;

    @Override
    public List<EventAnalysisRespVO> analyzeEvent(EventAnalysisReqVO reqVO) {
        String groupBy = StrUtil.isNotBlank(reqVO.getGroupBy()) ? reqVO.getGroupBy() : "stat_hour";
        
        // 注意：实际项目中，对于 groupBy 字段要做白名单校验，防止 SQL 注入！
        // 比如只允许 ["stat_hour", "browser", "province", "city", "platform_type"] 等。
        QueryWrapper<EventAnalysisDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.select(
                groupBy + " AS dimensionKey",
                "count(1) AS pv",
                "uniqExact(device_id) AS uv",
                "avg(request_time) AS avgRequestTime"
        );
        
        if (StrUtil.isNotBlank(reqVO.getLogType())) {
            queryWrapper.eq("log_type", reqVO.getLogType());
        }
        
        if (StrUtil.isNotBlank(reqVO.getEventName())) {
            // 注意宽表里目前用的是 uri 或 request 作为具体事件的区分（如果是行为分析）
            queryWrapper.eq("request_uri", reqVO.getEventName());
        }

        // 时间过滤
        if (reqVO.getStartTime() != null) {
            queryWrapper.ge("start_time", reqVO.getStartTime());
        }
        if (reqVO.getEndTime() != null) {
            queryWrapper.le("start_time", reqVO.getEndTime());
        }

        queryWrapper.groupBy(groupBy);
        queryWrapper.orderByAsc(groupBy);

        log.info("【分析模块】执行 MyBatis-Plus OLAP SQL");

        // 使用 selectMaps 直接接收 Map 列表，完美适配动态 SELECT 字段
        List<Map<String, Object>> rows = eventAnalysisMapper.selectMaps(queryWrapper);
        List<EventAnalysisRespVO> result = new ArrayList<>(rows.size());
        
        for (Map<String, Object> row : rows) {
            EventAnalysisRespVO vo = new EventAnalysisRespVO();
            vo.setDimensionKey(String.valueOf(row.get("dimensionKey")));
            vo.setPv(((Number) row.get("pv")).longValue());
            vo.setUv(((Number) row.get("uv")).longValue());
            
            Object avgReqTimeObj = row.get("avgRequestTime");
            vo.setAvgRequestTime(avgReqTimeObj == null ? 0.0 : ((Number) avgReqTimeObj).doubleValue());
            
            result.add(vo);
        }

        return result;
    }
}
