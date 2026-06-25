package com.huanniankj.module.app.controller.alertrule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 告警规则 RespVO
 *
 * @author zhaoff
 */
@Data
@Schema(description = "告警规则响应")
public class AlertRuleRespVO {

    @Schema(description = "告警规则 ID", example = "1")
    private Long id;

    @Schema(description = "规则名称", example = "CPU使用率告警")
    private String name;

    @Schema(description = "告警类型", example = "1")
    private Integer alertType;

    @Schema(description = "指标名称", example = "cpu_usage")
    private String metricName;

    @Schema(description = "条件运算符", example = "gt")
    private String conditionOperator;

    @Schema(description = "阈值", example = "90")
    private String thresholdValue;

    @Schema(description = "持续时间（分钟）", example = "5")
    private Integer durationMinutes;

    @Schema(description = "通知类型", example = "email")
    private String notificationType;

    @Schema(description = "通知配置 (JSON 格式)", example = "{\"emails\":[\"admin@example.com\"]}")
    private String notificationConfig;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "备注", example = "生产环境CPU告警")
    private String remark;

    @Schema(description = "最后触发时间")
    private LocalDateTime lastTriggeredTime;

    @Schema(description = "触发次数", example = "10")
    private Integer triggerCount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
