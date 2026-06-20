package com.huanniankj.module.source.controller.database.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * 脚本执行请求 VO
 *
 * @author zhaoff
 */
@Data
@Schema(description = "脚本执行请求")
public class DatabaseScriptExecuteReqVO {

    @Schema(description = "脚本ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "脚本ID不能为空")
    private Long scriptId;

    @Schema(description = "是否持久化结果 (0-不持久化 1-持久化)", example = "1")
    private Integer persistResult;

    @Schema(description = "执行入参（参数名 -> 参数值）", example = "{\"userId\":1,\"startDate\":\"2024-01-01\"}")
    private Map<String, Object> inputParams;

}
