package com.huanniankj.module.system.controller.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * 验证码 Request VO
 *
 * @author zhaoff
 */
@Schema(description = "验证码 Request VO")
@Data
public class CaptchaVerificationReqVO {

    // ========== 图片验证码相关 ==========
    @Schema(description = "验证码，验证码开启时，需要传递", requiredMode = Schema.RequiredMode.REQUIRED, example = "ubax")
    @NotEmpty(message = "验证码不能为空", groups = CodeEnableGroup.class)
    private String captchaVerification;

    /**
     * 开启验证码的 Group
     */
    public interface CodeEnableGroup {
    }
}
