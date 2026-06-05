package com.huanniankj.module.gather.controller.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Agent 更新 Request VO
 *
 * @author zhaoff
 */
@Schema(description = "管理后台 - Agent 更新 Request VO")
@Data
public class AgentUpdateReqVO {

    @Schema(description = "Agent ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Agent ID 不能为空")
    private Long id;

    @Schema(description = "平台类型", example = "10")
    private Integer platform;

    @Schema(description = "配置信息（Vector 配置）", example = "{}")
    private String config;

    @Schema(description = "备注", example = "测试 Agent")
    private String remark;

}
