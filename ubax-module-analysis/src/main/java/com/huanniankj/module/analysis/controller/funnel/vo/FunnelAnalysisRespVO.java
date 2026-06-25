package com.huanniankj.module.analysis.controller.funnel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * @author zhaoff
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "漏斗分析响应")
public class FunnelAnalysisRespVO {

    @Schema(description = "漏斗步骤列表")
    private List<FunnelStep> steps;

    @Schema(description = "总用户数")
    private Long totalUsers;

    @Schema(description = "最终转化率(%)")
    private Double finalConversionRate;

    @Schema(description = "平均转化率(%)")
    private Double avgConversionRate;

    @Schema(description = "平均耗时(秒)")
    private Double avgDuration;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "漏斗步骤")
    public static class FunnelStep {

        @Schema(description = "步骤名称")
        private String stepName;

        @Schema(description = "用户数")
        private Long users;

        @Schema(description = "步骤转化率(%)")
        private Double conversionRate;

        @Schema(description = "总转化率(%)")
        private Double overallRate;

        @Schema(description = "流失率(%)")
        private Double lossRate;

        @Schema(description = "平均耗时(秒)")
        private Double avgTime;
    }
}
