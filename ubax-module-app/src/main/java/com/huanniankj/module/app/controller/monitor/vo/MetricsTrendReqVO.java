package com.huanniankj.module.app.controller.monitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 指标趋势查询 ReqVO
 *
 * @author zhaoff
 */
@Data
@Schema(description = "指标趋势查询请求")
public class MetricsTrendReqVO {

    @Schema(description = "指标名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "cpu_usage")
    @NotBlank(message = "指标名称不能为空")
    private String metricName;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "时间间隔", example = "1h")
    private String interval = "1h";

}
