package com.huanniankj.module.processing.controller.log.vo;

import com.huanniankj.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 异常日志分页 Request VO
 *
 * @author zhaoff
 */
@Schema(description = "异常日志分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class ErrorLogPageReqVO extends PageParam {

    @Schema(description = "异常类型", example = "format_error")
    private String errorType;

    @Schema(description = "数据来源，模糊匹配", example = "sdk")
    private String source;

    @Schema(description = "处理状态（0=待处理 1=已处理 2=已忽略）", example = "0")
    private Integer status;

    @Schema(description = "关联清洗管道ID", example = "2048")
    private Long pipelineId;

    @Schema(description = "关联事件ID", example = "3072")
    private Long eventId;

}
