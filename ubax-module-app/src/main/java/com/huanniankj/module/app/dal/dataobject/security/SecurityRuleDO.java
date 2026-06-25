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
 * 安全检测规则 DO
 *
 * @author zhaoff
 */
@TableName("app_security_rule")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityRuleDO extends BaseDO {

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
     * 检测类型: 1=SQL注入 2=XSS攻击 3=暴力破解 4=异常访问 5=自定义
     */
    private Integer detectionType;

    /**
     * 检测模式(JSON)
     */
    private String pattern;

    /**
     * 严重等级: 1=低 2=中 3=高 4=严重
     */
    private Integer severity;

    /**
     * 处置动作: alert/block/log
     */
    private String action;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 触发次数
     */
    private Long triggerCount;

    /**
     * 最后触发时间
     */
    private LocalDateTime lastTriggeredTime;

    /**
     * 备注
     */
    private String remark;

}
