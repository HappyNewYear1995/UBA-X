package com.huanniankj.module.analysis.controller.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EventAnalysisReqVO {

    /**
     * 分析的事件名称（如 $pageview, button_click）
     */
    private String eventName;

    /**
     * 分析的日志类型（如 AccessLog, ClickLog）
     */
    private String logType;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 聚合维度字段 (如 stat_hour, browser, province 等)
     */
    private String groupBy;
}
