package com.huanniankj.module.app.dal.dataobject.alert;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.huanniankj.framework.mybatis.core.dataobject.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 告警规则 DO
 *
 * @author zhaoff
 */
@TableName("app_alert_rule")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertRuleDO extends BaseDO {

    /**
     * 规则ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 规则名称
     */
    private String name;

    /**
     * 告警类型: 1=指标阈值 2=波动检测 3=异常检测
     */
    private Integer alertType;

    /**
     * 监控指标名称
     */
    private String metricName;

    /**
     * 条件运算符: gt/lt/gte/lte/eq/neq
     */
    private String conditionOperator;

    /**
     * 阈值
     */
    private String thresholdValue;

    /**
     * 持续时间(分钟)
     */
    private Integer durationMinutes;

    /**
     * 通知方式: email/dingtalk/wechat/webhook
     */
    private String notificationType;

    /**
     * 通知配置(JSON)
     */
    private String notificationConfig;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 最后触发时间
     */
    private LocalDateTime lastTriggeredTime;

    /**
     * 触发次数
     */
    private Long triggerCount;

    /**
     * 备注
     */
    private String remark;

}
