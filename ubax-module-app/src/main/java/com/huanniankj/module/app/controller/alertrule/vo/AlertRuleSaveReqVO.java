package com.huanniankj.module.app.controller.alertrule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 告警规则保存 ReqVO
 *
 * @author zhaoff
 */
@Data
@Schema(description = "告警规则保存请求")
public class AlertRuleSaveReqVO {

    @Schema(description = "告警规则 ID", example = "1")
    private Long id;

    @Schema(description = "规则名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "CPU使用率告警")
    @NotBlank(message = "规则名称不能为空")
    @Size(max = 100, message = "规则名称长度不能超过100个字符")
    private String name;

    @Schema(description = "告警类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "告警类型不能为空")
    private Integer alertType;

    @Schema(description = "指标名称", example = "cpu_usage")
    @Size(max = 100, message = "指标名称长度不能超过100个字符")
    private String metricName;

    @Schema(description = "条件运算符", example = "gt")
    @Size(max = 20, message = "条件运算符长度不能超过20个字符")
    private String conditionOperator;

    @Schema(description = "阈值", example = "90")
    @Size(max = 200, message = "阈值长度不能超过200个字符")
    private String thresholdValue;

    @Schema(description = "持续时间（分钟）", example = "5")
    private Integer durationMinutes;

    @Schema(description = "通知类型", example = "email")
    @Size(max = 50, message = "通知类型长度不能超过50个字符")
    private String notificationType;

    @Schema(description = "通知配置 (JSON 格式)", example = "{\"emails\":[\"admin@example.com\"]}")
    private String notificationConfig;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "备注", example = "生产环境CPU告警")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

}
