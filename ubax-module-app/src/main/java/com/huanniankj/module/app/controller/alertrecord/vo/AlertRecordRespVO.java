package com.huanniankj.module.app.controller.alertrecord.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 告警记录 RespVO
 *
 * @author zhaoff
 */
@Data
@Schema(description = "告警记录响应")
public class AlertRecordRespVO {

    @Schema(description = "告警记录 ID", example = "1")
    private Long id;

    @Schema(description = "规则 ID", example = "1")
    private Long ruleId;

    @Schema(description = "规则名称", example = "CPU使用率告警")
    private String ruleName;

    @Schema(description = "告警类型", example = "1")
    private Integer alertType;

    @Schema(description = "告警级别", example = "1")
    private Integer alertLevel;

    @Schema(description = "指标值", example = "95.5")
    private String metricValue;

    @Schema(description = "阈值", example = "90")
    private String thresholdValue;

    @Schema(description = "告警消息", example = "CPU使用率超过阈值")
    private String message;

    @Schema(description = "通知状态", example = "0")
    private Integer notificationStatus;

    @Schema(description = "是否已确认", example = "false")
    private Boolean acknowledged;

    @Schema(description = "确认人", example = "admin")
    private String acknowledgedBy;

    @Schema(description = "确认时间")
    private LocalDateTime acknowledgedTime;

    @Schema(description = "备注", example = "已处理")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
