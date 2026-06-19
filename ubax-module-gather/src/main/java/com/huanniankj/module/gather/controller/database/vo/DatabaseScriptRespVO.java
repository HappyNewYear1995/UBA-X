package com.huanniankj.module.gather.controller.database.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据库脚本响应 VO
 *
 * @author zhaoff
 */
@Data
@Schema(description = "数据库脚本响应")
public class DatabaseScriptRespVO {

    @Schema(description = "脚本ID", example = "1")
    private Long id;

    @Schema(description = "脚本名称", example = "用户数据采集")
    private String name;

    @Schema(description = "脚本编码", example = "user_data_sync")
    private String code;

    @Schema(description = "数据源ID", example = "1")
    private Long databaseId;

    @Schema(description = "数据源名称")
    private String dataSourceName;

    @Schema(description = "脚本类型", example = "sql")
    private String scriptType;

    @Schema(description = "脚本类型名称", example = "SQL脚本")
    private String scriptTypeName;

    @Schema(description = "脚本内容")
    private String scriptContent;

    @Schema(description = "脚本描述", example = "从用户表采集数据")
    private String description;

    @Schema(description = "结果映射表名", example = "gather_data_result")
    private String resultTableName;

    @Schema(description = "结果字段映射 (JSON格式)")
    private String resultFieldMapping;

    @Schema(description = "Cron表达式", example = "0 0 * * * ?")
    private String cronExpression;

    @Schema(description = "执行次数", example = "10")
    private Integer executeCount;

    @Schema(description = "最后执行时间")
    private LocalDateTime lastExecuteTime;

    @Schema(description = "最后执行状态 (0-成功 1-失败)", example = "0")
    private Integer lastExecuteStatus;

    @Schema(description = "状态 (0-启用 1-禁用)", example = "0")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
