package com.huanniankj.module.gather.controller.database.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 脚本保存请求 VO
 *
 * @author zhaoff
 */
@Data
@Schema(description = "脚本保存请求")
public class DatabaseScriptSaveReqVO {

    @Schema(description = "脚本ID (更新时必填)", example = "1")
    private Long id;

    @Schema(description = "脚本名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "用户数据采集")
    @NotBlank(message = "脚本名称不能为空")
    private String name;

    @Schema(description = "脚本编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "user_data_sync")
    @NotBlank(message = "脚本编码不能为空")
    private String code;

    @Schema(description = "数据源ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "数据源ID不能为空")
    private Long databaseId;

    @Schema(description = "脚本类型 (sql/procedure/view)", requiredMode = Schema.RequiredMode.REQUIRED, example = "sql")
    @NotBlank(message = "脚本类型不能为空")
    private String scriptType;

    @Schema(description = "脚本内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "脚本内容不能为空")
    private String scriptContent;

    @Schema(description = "脚本描述", example = "从用户表采集数据")
    private String description;

    @Schema(description = "结果映射表名", example = "gather_data_result")
    private String resultTableName;

    @Schema(description = "结果字段映射 (JSON格式)", example = "{\"id\":\"user_id\",\"name\":\"user_name\"}")
    private String resultFieldMapping;

    @Schema(description = "Cron表达式", example = "0 0 * * * ?")
    private String cronExpression;

    @Schema(description = "状态 (0-启用 1-禁用)", example = "0")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

}
