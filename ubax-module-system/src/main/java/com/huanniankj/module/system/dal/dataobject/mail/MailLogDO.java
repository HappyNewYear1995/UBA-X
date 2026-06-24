package com.huanniankj.module.system.dal.dataobject.mail;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.huanniankj.framework.common.enums.UserTypeEnum;
import com.huanniankj.framework.mybatis.core.dataobject.BaseDO;
import com.huanniankj.framework.mybatis.core.type.StringListTypeHandler;
import com.huanniankj.framework.tenant.core.aop.TenantIgnore;
import com.huanniankj.module.system.enums.mail.MailSendStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 邮箱日志 DO
 * 记录每一次邮件的发送
 *
 * @author zhaoff
 */
@TableName(value = "system_mail_log", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TenantIgnore
public class MailLogDO extends BaseDO implements Serializable {

    /**
     * 日志编号，自增
     */
    private Long id;

    /**
     * 用户编码
     */
    private Long userId;

    /**
     * 用户类型
     * <p>
     * 枚举 {@link UserTypeEnum}
     */
    private Integer userType;

    /**
     * 接收邮箱地址
     */
    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> toMails;

    /**
     * 接收邮箱地址
     */
    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> ccMails;

    /**
     * 密送邮箱地址
     */
    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> bccMails;

    /**
     * 邮箱账号编号
     * <p>
     * 关联 {@link MailAccountDO#getId()}
     */
    private Long accountId;

    /**
     * 发送邮箱地址
     * <p>
     * 冗余 {@link MailAccountDO#getMail()}
     */
    private String fromMail;

    // ========= 模板相关字段 =========
    /**
     * 模版编号
     * <p>
     * 关联 {@link MailTemplateDO#getId()}
     */
    private Long templateId;

    /**
     * 模版编码
     * <p>
     * 冗余 {@link MailTemplateDO#getCode()}
     */
    private String templateCode;

    /**
     * 模版发送人名称
     * <p>
     * 冗余 {@link MailTemplateDO#getNickname()}
     */
    private String templateNickname;

    /**
     * 模版标题
     */
    private String templateTitle;

    /**
     * 模版内容
     * <p>
     * 基于 {@link MailTemplateDO#getContent()} 格式化后的内容
     */
    private String templateContent;

    /**
     * 模版参数
     * <p>
     * 基于 {@link MailTemplateDO#getParams()} 输入后的参数
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> templateParams;

    // ========= 发送相关字段 =========

    /**
     * 发送状态
     * <p>
     * 枚举 {@link MailSendStatusEnum}
     */
    private Integer sendStatus;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

    /**
     * 发送返回的消息 ID
     */
    private String sendMessageId;

    /**
     * 发送异常
     */
    private String sendException;

}
