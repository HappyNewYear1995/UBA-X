package com.huanniankj.module.system.api.social.dto;

import com.huanniankj.framework.common.enums.UserTypeEnum;
import com.huanniankj.framework.common.validation.InEnum;
import com.huanniankj.module.system.enums.social.SocialTypeEnum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 取消绑定社交用户 Request DTO
 *
 * @author zhaoff
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocialUserBindReqDTO {

    /**
     * 用户编号
     */
    @NotNull(message = "用户编号不能为空")
    private Long userId;

    /**
     * 用户类型
     */
    @InEnum(UserTypeEnum.class)
    @NotNull(message = "用户类型不能为空")
    private Integer userType;

    /**
     * 社交平台的类型
     */
    @InEnum(SocialTypeEnum.class)
    @NotNull(message = "社交平台的类型不能为空")
    private Integer socialType;

    /**
     * 授权码
     */
    @NotEmpty(message = "授权码不能为空")
    private String code;

    /**
     * state
     */
    @NotNull(message = "state 不能为空")
    private String state;

}
