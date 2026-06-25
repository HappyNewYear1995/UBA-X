package com.huanniankj.module.app.controller.monitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 指标趋势 RespVO
 *
 * @author zhaoff
 */
@Data
@Schema(description = "指标趋势响应")
public class MetricsTrendRespVO {

    @Schema(description = "时间点")
    private LocalDateTime time;

    @Schema(description = "指标值", example = "85.5")
    private Double value;

}
