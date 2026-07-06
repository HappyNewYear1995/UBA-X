package com.huanniankj.module.source.controller.processing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "处理脚本响应")
public class ProcessingScriptRespVO {

    @Schema(description = "脚本ID")
    private Long id;

    @Schema(description = "脚本名称")
    private String name;

    @Schema(description = "脚本编码")
    private String code;

    @Schema(description = "Groovy 脚本内容")
    private String scriptContent;

    @Schema(description = "脚本描述")
    private String description;

    @Schema(description = "入参定义 (JSON格式)")
    private String inputParams;

    @Schema(description = "结果持久化表名")
    private String resultTableName;

    @Schema(description = "结果字段映射")
    private String resultFieldMapping;

    @Schema(description = "Cron表达式")
    private String cronExpression;

    @Schema(description = "执行次数")
    private Integer executeCount;

    @Schema(description = "最后执行时间")
    private LocalDateTime lastExecuteTime;

    @Schema(description = "最后执行状态 (0-成功 1-失败)")
    private Integer lastExecuteStatus;

    @Schema(description = "状态 (0-启用 1-禁用)")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
