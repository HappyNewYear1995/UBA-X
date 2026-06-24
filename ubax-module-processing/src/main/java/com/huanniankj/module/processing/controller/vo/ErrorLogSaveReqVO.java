package com.huanniankj.module.processing.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 异常日志创建/修改 Request VO
 *
 * @author zhaoff
 */
@Schema(description = "异常日志创建/修改 Request VO")
@Data
public class ErrorLogSaveReqVO {

    @Schema(description = "日志 ID", example = "1024")
    private Long id;

    @Schema(description = "异常类型（format_error/duplicate_data/invalid_char/timestamp_error/other）", example = "format_error")
    private String errorType;

    @Schema(description = "数据来源", example = "sdk")
    private String source;

    @Schema(description = "异常记录数", example = "10")
    private Integer errorCount;

    @Schema(description = "处理动作（auto_filter/auto_dedup/mark_pending/manual_handle）", example = "auto_filter")
    private String action;

    @Schema(description = "异常详情（JSON格式）")
    private String detail;

    @Schema(description = "关联清洗管道ID", example = "2048")
    private Long pipelineId;

    @Schema(description = "关联事件ID", example = "3072")
    private Long eventId;

    @Schema(description = "处理状态（0=待处理 1=已处理 2=已忽略）", example = "0")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

}
