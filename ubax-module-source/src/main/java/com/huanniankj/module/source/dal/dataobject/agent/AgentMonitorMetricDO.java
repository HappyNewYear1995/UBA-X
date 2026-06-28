package com.huanniankj.module.source.dal.dataobject.agent;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.huanniankj.framework.mybatis.core.dataobject.BaseDO;
import com.huanniankj.module.source.enums.agent.MetricTypeEnum;
import com.huanniankj.module.source.enums.agent.TimeGranularityEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 运行监控指标 DO
 *
 * @author zhaoff
 */
@TableName("source_monitor_metric")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentMonitorMetricDO extends BaseDO {

    /**
     * 指标 ID
     */
    @TableId
    private Long id;

    /**
     * Agent ID
     */
    private Long agentId;

    /**
     * 指标类型
     * <p>
     * 枚举 {@link MetricTypeEnum}
     */
    private String metricType;

    /**
     * 指标名称
     */
    private String metricName;

    /**
     * 指标值
     */
    private Double metricValue;

    /**
     * 指标单位
     */
    private String metricUnit;

    /**
     * 时间粒度
     * <p>
     * 枚举 {@link TimeGranularityEnum}
     */
    private String timeGranularity;

    /**
     * 统计开始时间
     */
    private LocalDateTime startTime;

    /**
     * 统计结束时间
     */
    private LocalDateTime endTime;

    /**
     * 指标数据时间
     */
    private LocalDateTime metricTime;

    /**
     * 额外属性，JSON 格式
     */
    private String extraData;

}
