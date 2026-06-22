package com.huanniankj.module.analysis.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "留存分析配置保存请求")
public class RetentionConfigSaveReqVO {

    @Schema(description = "配置 ID", example = "1")
    private Long id;

    @Schema(description = "配置名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "次日留存分析")
    @NotBlank(message = "配置名称不能为空")
    private String name;

    @Schema(description = "留存类型 (next_day/7_days/30_days)", requiredMode = Schema.RequiredMode.REQUIRED, example = "next_day")
    @NotBlank(message = "留存类型不能为空")
    private String retentionType;

    @Schema(description = "统计维度 (day/week/month)", example = "day")
    private String dimension;

    @Schema(description = "起始事件", example = "app_open")
    private String startEvent;

    @Schema(description = "回访事件", example = "page_view")
    private String returnEvent;

    @Schema(description = "备注")
    private String remark;

}
