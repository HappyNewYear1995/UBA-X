package com.huanniankj.module.analysis.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "漏斗分析请求")
public class FunnelAnalysisReqVO {

    @Schema(description = "漏斗步骤事件列表（按顺序）", requiredMode = Schema.RequiredMode.REQUIRED, example = "[\"app_open\",\"home_view\",\"product_detail\"]")
    @NotEmpty(message = "漏斗步骤不能为空")
    private List<String> steps;

    @Schema(description = "开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

}
