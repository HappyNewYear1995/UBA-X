package com.huanniankj.module.gather.controller.admin.monitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 运行监控指标响应 VO
 *
 * @author zhaoff
 */
@Schema(description = "管理后台 - 运行监控指标响应 VO")
@Data
public class MonitorMetricRespVO {

    @Schema(description = "指标 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "Agent UUID", requiredMode = Schema.RequiredMode.REQUIRED, example = "agent-uuid-001")
    private String agentUuid;

    @Schema(description = "指标类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "event_count")
    private String metricType;

    @Schema(description = "指标类型名", example = "事件数量")
    private String metricTypeName;

    @Schema(description = "指标名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "events_per_minute")
    private String metricName;

    @Schema(description = "指标值", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.5")
    private Double metricValue;

    @Schema(description = "指标单位", example = "次/分钟")
    private String metricUnit;

    @Schema(description = "时间粒度", example = "minute")
    private String timeGranularity;

    @Schema(description = "时间粒度名", example = "分钟")
    private String timeGranularityName;

    @Schema(description = "统计开始时间")
    private LocalDateTime startTime;

    @Schema(description = "统计结束时间")
    private LocalDateTime endTime;

    @Schema(description = "指标数据时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime metricTime;

    @Schema(description = "额外属性，JSON 格式")
    private String extraData;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
