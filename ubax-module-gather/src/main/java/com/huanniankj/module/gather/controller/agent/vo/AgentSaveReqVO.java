package com.huanniankj.module.gather.controller.agent.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Agent创建/修改 Request VO
 *
 * @author zhaoff
 */
@Schema(description = "Agent创建/修改 Request VO")
@Data
public class AgentSaveReqVO {

    @JsonProperty("uuid")
    @Schema(description = "UUID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "UUID不能为空")
    private String uuid;

    @JsonProperty("hostname")
    @Schema(description = "主机名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String hostname;

    @JsonProperty("version")
    @Schema(description = "Agent 版本", requiredMode = Schema.RequiredMode.REQUIRED)
    private String version;

    @JsonProperty("terminal")
    @Schema(description = "终端类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String terminal;

    @JsonProperty("ip")
    @Schema(description = "IP", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ip;

    @JsonProperty("os")
    @Schema(description = "操作系统", requiredMode = Schema.RequiredMode.REQUIRED)
    private String os;

}
