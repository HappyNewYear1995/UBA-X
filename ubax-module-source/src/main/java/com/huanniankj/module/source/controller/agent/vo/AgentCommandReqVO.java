package com.huanniankj.module.source.controller.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * Agent 命令推送 ReqVO
 *
 * @author zhaoff
 */
@Schema(description = "Agent 命令推送 Request VO")
@Data
public class AgentCommandReqVO {

    @Schema(description = "UUID", requiredMode = Schema.RequiredMode.REQUIRED, example = "7e9e362a-5d29-4a23-ab90-3ad02ea54e9e")
    @NotBlank(message = "UUID不能为空")
    private String uuid;

    @Schema(description = "命令动作", requiredMode = Schema.RequiredMode.REQUIRED, example = "restart")
    @NotBlank(message = "命令动作不能为空")
    private String action;

    @Schema(description = "命令参数", example = "{\"force\": true}")
    private Map<String, Object> params;

}
