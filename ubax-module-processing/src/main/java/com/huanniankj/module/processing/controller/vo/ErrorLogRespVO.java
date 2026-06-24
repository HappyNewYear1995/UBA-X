package com.huanniankj.module.processing.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 异常日志信息 Response VO
 *
 * @author zhaoff
 */
@Schema(description = "异常日志信息 Response VO")
@Data
public class ErrorLogRespVO {

    @Schema(description = "日志 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
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

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
