package com.huanniankj.module.analysis.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "漏斗分析结果响应")
public class FunnelResultRespVO {

    @Schema(description = "结果 ID")
    private Long id;

    @Schema(description = "漏斗配置 ID")
    private Long configId;

    @Schema(description = "统计日期")
    private LocalDate statDate;

    @Schema(description = "总用户数")
    private Long totalUsers;

    @Schema(description = "最终转化率(%)")
    private Double finalConversionRate;

    @Schema(description = "平均转化率(%)")
    private Double avgConversionRate;

    @Schema(description = "平均耗时(秒)")
    private Double avgDuration;

    @Schema(description = "漏斗步骤结果列表")
    private List<FunnelStepResult> steps;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "漏斗步骤结果")
    public static class FunnelStepResult {

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
