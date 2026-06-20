package com.huanniankj.module.source.controller.database.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * SQL 执行响应 VO
 *
 * @author zhaoff
 */
@Data
@Schema(description = "SQL 执行响应")
public class DatabaseSqlExecuteRespVO {

    @Schema(description = "执行是否成功")
    private Boolean success;

    @Schema(description = "执行耗时 (毫秒)")
    private Long costTime;

    @Schema(description = "结果集列表 (支持多结果集)")
    private List<Map<String, Object>> results;

    @Schema(description = "多结果集列表，每个元素为一个独立结果集")
    private List<List<Map<String, Object>>> resultSetList;

    @Schema(description = "结果集列信息，每个元素为一个结果集的列名列表")
    private List<List<String>> resultSetColumns;

    @Schema(description = "影响的行数 (INSERT/UPDATE/DELETE)")
    private Integer affectedRows;

    @Schema(description = "错误信息")
    private String errorMessage;

}
