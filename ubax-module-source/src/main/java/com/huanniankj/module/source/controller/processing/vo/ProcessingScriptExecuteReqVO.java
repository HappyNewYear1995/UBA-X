package com.huanniankj.module.source.controller.processing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
@Schema(description = "处理脚本执行请求")
public class ProcessingScriptExecuteReqVO {

    @Schema(description = "脚本ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "脚本ID不能为空")
    private Long scriptId;

    @Schema(description = "是否持久化结果 (1-是 0-否)")
    private Integer persistResult;

    @Schema(description = "执行入参")
    private Map<String, Object> inputParams;

}
