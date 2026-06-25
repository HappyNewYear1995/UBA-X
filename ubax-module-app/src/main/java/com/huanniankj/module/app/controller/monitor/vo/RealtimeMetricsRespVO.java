package com.huanniankj.module.app.controller.monitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实时监控指标 RespVO
 *
 * @author zhaoff
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "实时监控指标响应")
public class RealtimeMetricsRespVO {

    @Schema(description = "活跃用户数", example = "128")
    private Long activeUsers;

    @Schema(description = "页面浏览量", example = "1024")
    private Long pageViews;

    @Schema(description = "转化率", example = "0.85")
    private Double conversionRate;

    @Schema(description = "异常事件数", example = "3")
    private Long anomalyEvents;

}
