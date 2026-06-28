package com.huanniankj.module.source.controller.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 运行监控指标保存请求 VO
 *
 * @author zhaoff
 */
@Schema(description = "运行监控指标保存请求 VO")
@Data
public class MonitorMetricSaveReqVO {

    @Schema(description = "Agent UUID", requiredMode = Schema.RequiredMode.REQUIRED, example = "agent-uuid-001")
    @NotBlank(message = "Agent UUID 不能为空")
    private String agentUuid;

    @Schema(description = "指标类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "event_count")
    @NotBlank(message = "指标类型不能为空")
    private String metricType;

    @Schema(description = "指标名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "events_per_minute")
    @NotBlank(message = "指标名称不能为空")
    private String metricName;

    @Schema(description = "指标值", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.5")
    @NotNull(message = "指标值不能为空")
    private Double metricValue;

    @Schema(description = "指标单位", example = "次/分钟")
    private String metricUnit;

    @Schema(description = "时间粒度", example = "minute")
    private String timeGranularity;

    @Schema(description = "统计开始时间")
    private LocalDateTime startTime;

    @Schema(description = "统计结束时间")
    private LocalDateTime endTime;

    @Schema(description = "指标数据时间")
    private LocalDateTime metricTime;

    @Schema(description = "额外属性，JSON 格式")
    private String extraData;

}
