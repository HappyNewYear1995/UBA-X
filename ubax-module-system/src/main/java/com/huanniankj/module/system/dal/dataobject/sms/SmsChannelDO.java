package com.huanniankj.module.system.dal.dataobject.sms;

import com.baomidou.mybatisplus.annotation.TableName;
import com.huanniankj.framework.common.enums.CommonStatusEnum;
import com.huanniankj.framework.mybatis.core.dataobject.BaseDO;
import com.huanniankj.framework.tenant.core.aop.TenantIgnore;
import com.huanniankj.module.system.framework.sms.core.enums.SmsChannelEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 短信渠道 DO
 *
 * @author zhaoff
 */
@TableName(value = "system_sms_channel", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TenantIgnore
public class SmsChannelDO extends BaseDO {

    /**
     * 渠道编号
     */
    private Long id;

    /**
     * 短信签名
     */
    private String signature;

    /**
     * 渠道编码
     * <p>
     * 枚举 {@link SmsChannelEnum}
     */
    private String code;

    /**
     * 启用状态
     * <p>
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 短信 API 的账号
     */
    private String apiKey;

    /**
     * 短信 API 的密钥
     */
    private String apiSecret;

    /**
     * 短信发送回调 URL
     */
    private String callbackUrl;

}
