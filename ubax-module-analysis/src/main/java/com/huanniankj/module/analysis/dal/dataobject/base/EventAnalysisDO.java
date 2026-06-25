package com.huanniankj.module.analysis.dal.dataobject.base;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.huanniankj.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 分析模块使用的数据对象
 * 映射 ClickHouse 的 ubax_user_behavior_log 表
 *
 * @author zhaoff
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ubax_user_behavior_log")
public class EventAnalysisDO extends BaseDO {

    @TableId(type = IdType.ASSIGN_UUID)
    private String logId;

    private String logType;
    private String requestUri;
    private LocalDateTime startTime;
    private String deviceId;
    private Double requestTime;

    // 以下为可能的维度字段
    private String browser;
    private String province;
    private String city;
    private String platformType;
    private Long statHour;
}
