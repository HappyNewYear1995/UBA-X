package com.huanniankj.module.source.controller.processing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "处理脚本保存请求")
public class ProcessingScriptSaveReqVO {

    @Schema(description = "脚本ID (更新时必填)", example = "1")
    private Long id;

    @Schema(description = "脚本名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "用户数据同步处理")
    @NotBlank(message = "脚本名称不能为空")
    private String name;

    @Schema(description = "脚本编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "user_sync_process")
    @NotBlank(message = "脚本编码不能为空")
    private String code;

    @Schema(description = "Groovy 脚本内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "脚本内容不能为空")
    private String scriptContent;

    @Schema(description = "脚本描述")
    private String description;

    @Schema(description = "入参定义 (JSON格式)", example = "[{\"name\":\"userId\",\"type\":\"Long\",\"required\":true}]")
    private String inputParams;

    @Schema(description = "结果持久化表名")
    private String resultTableName;

    @Schema(description = "结果字段映射 (JSON格式)")
    private String resultFieldMapping;

    @Schema(description = "Cron表达式")
    private String cronExpression;

    @Schema(description = "状态 (0-启用 1-禁用)")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

}
