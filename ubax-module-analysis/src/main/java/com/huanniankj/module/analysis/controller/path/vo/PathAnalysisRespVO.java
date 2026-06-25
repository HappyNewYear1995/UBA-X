package com.huanniankj.module.analysis.controller.path.vo;

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
@Schema(description = "路径分析响应")
public class PathAnalysisRespVO {

    @Schema(description = "桑基图节点列表")
    private List<SankeyNode> nodes;

    @Schema(description = "桑基图链接列表")
    private List<SankeyLink> links;

    @Schema(description = "路径统计列表")
    private List<PathStat> pathStats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "桑基图节点")
    public static class SankeyNode {

        @Schema(description = "节点名称")
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "桑基图链接")
    public static class SankeyLink {

        @Schema(description = "源节点")
        private String source;

        @Schema(description = "目标节点")
        private String target;

        @Schema(description = "流量值")
        private Long value;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "路径统计")
    public static class PathStat {

        @Schema(description = "用户路径")
        private String path;

        @Schema(description = "用户数")
        private Long users;

        @Schema(description = "占比(%)")
        private Double percentage;

        @Schema(description = "平均耗时(秒)")
        private Double avgDuration;
    }
}
