package com.huanniankj.module.source.controller.processing.vo;

import com.huanniankj.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "处理脚本执行日志分页请求")
public class ProcessingScriptLogPageReqVO extends PageParam {

    @Schema(description = "脚本ID", example = "1")
    private Long scriptId;

    @Schema(description = "执行状态 (0-成功 1-失败)", example = "0")
    private Integer status;

    @Schema(description = "执行类型 (manual/scheduled)", example = "manual")
    private String executeType;

    @Schema(description = "开始时间")
    private LocalDateTime beginTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

}
