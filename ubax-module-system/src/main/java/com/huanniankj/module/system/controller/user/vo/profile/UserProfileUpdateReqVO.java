package com.huanniankj.module.system.controller.user.vo.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

/**
 * 用户个人中心更新密码 Request VO
 *
 * @author zhaoff
 */
@Schema(description = "c")
@Data
public class UserProfileUpdateReqVO {

    @Schema(description = "用户昵称", example = "ubax")
    @Size(max = 30, message = "用户昵称长度不能超过 30 个字符")
    private String nickname;

    @Schema(description = "用户邮箱", example = "ubax")
    @Email(message = "邮箱格式不正确")
    @Size(max = 50, message = "邮箱长度不能超过 50 个字符")
    private String email;

    @Schema(description = "手机号码", example = "12312341234")
    @Length(min = 11, max = 11, message = "手机号长度必须 11 位")
    private String mobile;

    @Schema(description = "用户性别，参见 SexEnum 枚举类", example = "1")
    private Integer sex;

    @Schema(description = "角色头像", example = "ubax")
    @URL(message = "头像地址格式不正确")
    private String avatar;

}
