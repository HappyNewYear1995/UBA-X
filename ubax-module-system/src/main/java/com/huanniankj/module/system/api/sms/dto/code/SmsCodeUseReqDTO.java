package com.huanniankj.module.system.api.sms.dto.code;

import com.huanniankj.framework.common.validation.InEnum;
import com.huanniankj.framework.common.validation.Mobile;
import com.huanniankj.module.system.enums.sms.SmsSceneEnum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 短信验证码的使用 Request DTO
 *
 * @author zhaoff
 */
@Data
public class SmsCodeUseReqDTO {

    /**
     * 手机号
     */
    @Mobile
    @NotEmpty(message = "手机号不能为空")
    private String mobile;

    /**
     * 发送场景
     */
    @NotNull(message = "发送场景不能为空")
    @InEnum(SmsSceneEnum.class)
    private Integer scene;

    /**
     * 验证码
     */
    @NotEmpty(message = "验证码")
    private String code;

    /**
     * 使用 IP
     */
    @NotEmpty(message = "使用 IP 不能为空")
    private String usedIp;

}
