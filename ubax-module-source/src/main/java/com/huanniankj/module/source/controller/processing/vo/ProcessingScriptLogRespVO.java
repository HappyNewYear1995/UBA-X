package com.huanniankj.module.source.controller.processing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "处理脚本执行日志响应")
public class ProcessingScriptLogRespVO {

    @Schema(description = "日志ID", example = "1")
    private Long id;

    @Schema(description = "脚本ID", example = "1")
    private Long scriptId;

    @Schema(description = "脚本名称", example = "数据同步处理")
    private String scriptName;

    @Schema(description = "脚本编码", example = "data_sync")
    private String scriptCode;

    @Schema(description = "执行类型", example = "manual")
    private String executeType;

    @Schema(description = "执行的脚本内容")
    private String scriptContent;

    @Schema(description = "执行入参 (JSON)")
    private String inputParams;

    @Schema(description = "执行状态 (0-成功 1-失败)", example = "0")
    private Integer status;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "执行耗时 (毫秒)", example = "500")
    private Long costTime;

    @Schema(description = "结果记录数", example = "100")
    private Long resultRecordCount;

    @Schema(description = "是否持久化 (0-未持久化 1-已持久化)", example = "1")
    private Integer persisted;

    @Schema(description = "持久化错误信息")
    private String persistError;

    @Schema(description = "执行结果 (JSON)")
    private String executeResult;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
