package com.huanniankj.module.system.dal.dataobject.mail;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.huanniankj.framework.common.enums.CommonStatusEnum;
import com.huanniankj.framework.mybatis.core.dataobject.BaseDO;
import com.huanniankj.framework.tenant.core.aop.TenantIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 邮件模版 DO
 *
 * @author zhaoff
 */
@TableName(value = "system_mail_template", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@TenantIgnore
public class MailTemplateDO extends BaseDO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 模版名称
     */
    private String name;

    /**
     * 模版编号
     */
    private String code;

    /**
     * 发送的邮箱账号编号
     * <p>
     * 关联 {@link MailAccountDO#getId()}
     */
    private Long accountId;

    /**
     * 发送人名称
     */
    private String nickname;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 参数数组(自动根据内容生成)
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> params;

    /**
     * 状态
     * <p>
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

}
