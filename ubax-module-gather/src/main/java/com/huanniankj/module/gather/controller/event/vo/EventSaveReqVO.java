package com.huanniankj.module.gather.controller.event.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 事件保存请求 VO
 *
 * @author zhaoff
 */
@Schema(description = "管理后台 - 事件保存请求 VO")
@Data
public class EventSaveReqVO {

    @Schema(description = "Agent UUID", requiredMode = Schema.RequiredMode.REQUIRED, example = "agent-uuid-001")
    @NotBlank(message = "Agent UUID 不能为空")
    private String agentUuid;

    @Schema(description = "事件类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "custom_event")
    @NotBlank(message = "事件类型不能为空")
    private String eventType;

    @Schema(description = "事件级别", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "事件级别不能为空")
    private Integer eventLevel;

    @Schema(description = "事件来源", example = "agent")
    private String eventSource;

    @Schema(description = "事件标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "CPU 使用率过高")
    @NotBlank(message = "事件标题不能为空")
    private String title;

    @Schema(description = "事件详情", example = "CPU 使用率超过 90%")
    private String content;

    @Schema(description = "事件标签，逗号分隔", example = "cpu,performance")
    private String tags;

    @Schema(description = "事件发生时间")
    private LocalDateTime eventTime;

}
