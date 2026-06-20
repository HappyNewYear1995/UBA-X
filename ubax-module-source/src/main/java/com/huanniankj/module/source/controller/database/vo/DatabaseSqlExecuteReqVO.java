package com.huanniankj.module.source.controller.database.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * SQL 执行请求 VO
 *
 * @author zhaoff
 */
@Data
@Schema(description = "SQL 执行请求")
public class DatabaseSqlExecuteReqVO {

    @Schema(description = "数据源 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "数据源 ID 不能为空")
    private Long databaseId;

    @Schema(description = "SQL 语句", requiredMode = Schema.RequiredMode.REQUIRED, example = "SELECT * FROM user LIMIT 10")
    @NotBlank(message = "SQL 语句不能为空")
    private String sql;

}
