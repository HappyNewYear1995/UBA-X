package com.huanniankj.module.gather.controller.database.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 存储过程执行请求 VO
 *
 * @author zhaoff
 */
@Data
@Schema(description = "存储过程执行请求")
public class DatabaseProcedureReqVO {

    @Schema(description = "数据源 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "数据源 ID 不能为空")
    private Long databaseId;

    @Schema(description = "存储过程名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "sp_get_user_list")
    @NotBlank(message = "存储过程名称不能为空")
    private String procedureName;

    @Schema(description = "输入参数列表", example = "[\"param1\", 123]")
    private List<Object> inputParams;

    @Schema(description = "输出参数名称列表 (可选)", example = "[\"out_count\"]")
    private List<String> outputParamNames;

}
