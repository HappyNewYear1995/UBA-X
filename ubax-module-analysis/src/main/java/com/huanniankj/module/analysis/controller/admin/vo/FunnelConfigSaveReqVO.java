package com.huanniankj.module.analysis.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "漏斗分析配置保存请求")
public class FunnelConfigSaveReqVO {

    @Schema(description = "配置 ID", example = "1")
    private Long id;

    @Schema(description = "漏斗名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "注册到支付漏斗")
    @NotBlank(message = "漏斗名称不能为空")
    private String name;

    @Schema(description = "漏斗步骤事件列表（JSON 数组格式）", requiredMode = Schema.RequiredMode.REQUIRED, example = "[\"app_open\",\"home_view\",\"product_detail\"]")
    @NotBlank(message = "漏斗步骤不能为空")
    private String steps;

    @Schema(description = "窗口时间（秒）", example = "86400")
    private Integer windowTime;

    @Schema(description = "备注", example = "注册到支付的完整转化漏斗")
    private String remark;

}
