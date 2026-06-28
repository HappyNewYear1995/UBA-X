package com.huanniankj.module.source.controller.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 监控统计响应 VO
 *
 * @author zhaoff
 */
@Schema(description = "监控统计响应 VO")
@Data
public class MonitorStatisticsRespVO {

    @Schema(description = "Agent UUID", requiredMode = Schema.RequiredMode.REQUIRED, example = "agent-uuid-001")
    private String agentUuid;

    @Schema(description = "事件总数", example = "1000")
    private Long totalEventCount;

    @Schema(description = "成功事件数", example = "950")
    private Long successEventCount;

    @Schema(description = "失败事件数", example = "50")
    private Long failedEventCount;

    @Schema(description = "成功率", example = "95.0")
    private Double successRate;

    @Schema(description = "错误率", example = "5.0")
    private Double errorRate;

    @Schema(description = "平均延迟（毫秒）", example = "120.5")
    private Double avgLatency;

    @Schema(description = "吞吐量（事件/秒）", example = "50.0")
    private Double throughput;

    @Schema(description = "带宽使用（MB）", example = "1024.5")
    private Double bandwidthUsage;

    @Schema(description = "各事件级别数量统计", example = "{\"1\": 500, \"2\": 300, \"3\": 150, \"4\": 50}")
    private Map<Integer, Long> eventLevelCountMap;

}
