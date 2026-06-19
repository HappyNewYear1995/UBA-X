package com.huanniankj.module.gather.controller.database.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 脚本执行响应 VO
 *
 * @author zhaoff
 */
@Data
@Schema(description = "脚本执行响应")
public class DatabaseScriptExecuteRespVO {

    @Schema(description = "是否成功", example = "true")
    private Boolean success;

    @Schema(description = "执行耗时 (毫秒)", example = "500")
    private Long costTime;

    @Schema(description = "结果数据列表")
    private List<Map<String, Object>> results;

    @Schema(description = "影响行数", example = "100")
    private Long affectedRows;

    @Schema(description = "输出参数 (存储过程用)")
    private Map<String, Object> outputParams;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "是否已持久化", example = "true")
    private Boolean persisted;

    @Schema(description = "结果记录数", example = "100")
    private Long resultRecordCount;

    @Schema(description = "持久化记录数", example = "100")
    private Long persistRecordCount;

}
