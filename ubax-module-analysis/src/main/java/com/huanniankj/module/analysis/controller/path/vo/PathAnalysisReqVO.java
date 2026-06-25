package com.huanniankj.module.analysis.controller.path.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * @author zhaoff
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "路径分析请求")
public class PathAnalysisReqVO {

    @Schema(description = "起始事件", example = "app_open")
    private String startEvent;

    @Schema(description = "开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @Schema(description = "最大路径深度", example = "7")
    private Integer maxDepth;

}
