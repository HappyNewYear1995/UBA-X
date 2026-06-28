package com.huanniankj.module.source.controller.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 事件响应 VO
 *
 * @author zhaoff
 */
@Schema(description = "事件响应 VO")
@Data
public class EventRespVO {

    @Schema(description = "事件 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "Agent UUID", requiredMode = Schema.RequiredMode.REQUIRED, example = "agent-uuid-001")
    private String agentUuid;

    @Schema(description = "事件类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "custom_event")
    private String eventType;

    @Schema(description = "事件级别", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer eventLevel;

    @Schema(description = "事件级别名", example = "信息")
    private String eventLevelName;

    @Schema(description = "事件来源", example = "agent")
    private String eventSource;

    @Schema(description = "事件来源名", example = "Agent 上报")
    private String eventSourceName;

    @Schema(description = "事件标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "CPU 使用率过高")
    private String title;

    @Schema(description = "事件详情", example = "CPU 使用率超过 90%")
    private String content;

    @Schema(description = "事件标签，逗号分隔", example = "cpu,performance")
    private String tags;

    @Schema(description = "事件发生时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime eventTime;

    @Schema(description = "是否已处理", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean handled;

    @Schema(description = "处理人", example = "admin")
    private String handler;

    @Schema(description = "处理时间")
    private LocalDateTime handleTime;

    @Schema(description = "处理备注", example = "已处理")
    private String handleRemark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
