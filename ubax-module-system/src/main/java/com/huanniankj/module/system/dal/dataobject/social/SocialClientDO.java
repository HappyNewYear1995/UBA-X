package com.huanniankj.module.system.dal.dataobject.social;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.huanniankj.framework.common.enums.CommonStatusEnum;
import com.huanniankj.framework.common.enums.UserTypeEnum;
import com.huanniankj.framework.tenant.core.db.TenantBaseDO;
import com.huanniankj.module.system.enums.social.SocialTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import me.zhyd.oauth.config.AuthConfig;

/**
 * 社交客户端 DO
 * <p>
 * 对应 {@link AuthConfig} 配置，满足不同租户，有自己的客户端配置，实现社交（三方）登录
 *
 * @author zhaoff
 */
@TableName(value = "system_social_client", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialClientDO extends TenantBaseDO {

    /**
     * 编号，自增
     */
    @TableId
    private Long id;

    /**
     * 应用名
     */
    private String name;

    /**
     * 社交类型
     * <p>
     * 枚举 {@link SocialTypeEnum}
     */
    private Integer socialType;

    /**
     * 用户类型
     * <p>
     * 目的：不同用户类型，对应不同的小程序，需要自己的配置
     * <p>
     * 枚举 {@link UserTypeEnum}
     */
    private Integer userType;

    /**
     * 状态
     * <p>
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

    /**
     * 客户端 id
     */
    private String clientId;

    /**
     * 客户端 Secret
     */
    private String clientSecret;

    /**
     * 代理编号
     * <p>
     * 目前只有部分“社交类型”在使用：
     * 1. 企业微信：对应授权方的网页应用 ID
     */
    private String agentId;

    /**
     * publicKey 公钥
     * <p>
     * 目前只有部分“社交类型”在使用：
     * 1. 支付宝：支付宝公钥
     */
    private String publicKey;

}
