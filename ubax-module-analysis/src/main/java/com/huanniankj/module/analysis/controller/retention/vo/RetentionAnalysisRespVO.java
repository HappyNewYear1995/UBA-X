package com.huanniankj.module.analysis.controller.retention.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * @author zhaoff
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "留存分析响应")
public class RetentionAnalysisRespVO {

    @Schema(description = "留存趋势数据")
    private List<TrendItem> trend;

    @Schema(description = "留存表格数据")
    private List<RetentionRow> table;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "留存趋势项")
    public static class TrendItem {

        @Schema(description = "日期")
        private String date;

        @Schema(description = "次日留存率(%)")
        private Double nextDayRate;

        @Schema(description = "7日留存率(%)")
        private Double day7Rate;

        @Schema(description = "30日留存率(%)")
        private Double day30Rate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "留存表格行")
    public static class RetentionRow {

        @Schema(description = "日期")
        private String date;

        @Schema(description = "新增用户数")
        private Long newUsers;

        @Schema(description = "各天留存率，key 为天数(1-30)，value 为留存率(%)")
        private Map<Integer, Double> retentionRates;
    }
}
