package com.huanniankj.module.source.controller.processing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Schema(description = "处理脚本执行响应")
public class ProcessingScriptExecuteRespVO {

    @Schema(description = "是否成功")
    private boolean success;

    @Schema(description = "耗时(ms)")
    private Long costTime;

    @Schema(description = "结果集")
    private List<Map<String, Object>> results;

    @Schema(description = "多结果集")
    private List<List<Map<String, Object>>> resultSetList;

    @Schema(description = "列名列表")
    private List<List<String>> resultSetColumns;

    @Schema(description = "结果记录数")
    private Long resultRecordCount;

    @Schema(description = "是否已持久化")
    private boolean persisted;

    @Schema(description = "持久化记录数")
    private long persistRecordCount;

    @Schema(description = "错误信息")
    private String errorMessage;

}
