package com.huanniankj.module.analysis.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "留存分析请求")
public class RetentionAnalysisReqVO {

    @Schema(description = "留存类型 (next_day/7_days/30_days)", requiredMode = Schema.RequiredMode.REQUIRED, example = "next_day")
    @NotNull(message = "留存类型不能为空")
    private String retentionType;

    @Schema(description = "统计维度 (day/week/month)", example = "day")
    private String dimension;

    @Schema(description = "开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "开始时间不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "结束时间不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

}
