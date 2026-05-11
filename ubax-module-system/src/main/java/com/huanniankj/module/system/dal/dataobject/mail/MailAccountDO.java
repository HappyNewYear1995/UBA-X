package com.huanniankj.module.system.dal.dataobject.mail;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.huanniankj.framework.mybatis.core.dataobject.BaseDO;
import com.huanniankj.framework.tenant.core.aop.TenantIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 邮箱账号 DO
 * <p>
 * 用途：配置发送邮箱的账号
 *
 * @author zhaoff
 */
@TableName(value = "system_mail_account", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@TenantIgnore
public class MailAccountDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 邮箱
     */
    private String mail;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * SMTP 服务器域名
     */
    private String host;

    /**
     * SMTP 服务器端口
     */
    private Integer port;

    /**
     * 是否开启 SSL
     */
    private Boolean sslEnable;

    /**
     * 是否开启 STARTTLS
     */
    private Boolean starttlsEnable;

}
