package com.huanniankj.module.app.dal.dataobject.security;

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
 * 安全检测事件 DO
 *
 * @author zhaoff
 */
@TableName("app_security_event")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityEventDO extends BaseDO {

    /**
     * 事件ID
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
     * 检测类型
     */
    private Integer detectionType;

    /**
     * 来源IP
     */
    private String sourceIp;

    /**
     * 目标资源
     */
    private String targetResource;

    /**
     * 事件详情(JSON)
     */
    private String eventDetail;

    /**
     * 严重等级
     */
    private Integer severity;

    /**
     * 执行的处置动作
     */
    private String actionTaken;

    /**
     * 是否已处理
     */
    private Boolean handled;

    /**
     * 处理人
     */
    private String handler;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 处理备注
     */
    private String handleRemark;

}
