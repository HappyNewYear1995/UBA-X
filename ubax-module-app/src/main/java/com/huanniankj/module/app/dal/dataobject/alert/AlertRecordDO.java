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
 * 告警记录 DO
 *
 * @author zhaoff
 */
@TableName("app_alert_record")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertRecordDO extends BaseDO {

    /**
     * 记录ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 规则ID
     */
    private Long ruleId;

    /**
     * 规则名称(冗余)
     */
    private String ruleName;

    /**
     * 告警类型
     */
    private Integer alertType;

    /**
     * 告警级别: 1=提示 2=警告 3=严重 4=紧急
     */
    private Integer alertLevel;

    /**
     * 实际指标值
     */
    private String metricValue;

    /**
     * 阈值
     */
    private String thresholdValue;

    /**
     * 告警消息
     */
    private String message;

    /**
     * 通知状态: 0=未通知 1=已通知 2=通知失败
     */
    private Integer notificationStatus;

    /**
     * 是否已确认
     */
    private Boolean acknowledged;

    /**
     * 确认人
     */
    private String acknowledgedBy;

    /**
     * 确认时间
     */
    private LocalDateTime acknowledgedTime;

    /**
     * 备注
     */
    private String remark;

}
