package com.huanniankj.module.system.dal.dataobject.social;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.huanniankj.framework.common.enums.UserTypeEnum;
import com.huanniankj.framework.mybatis.core.dataobject.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 社交用户的绑定
 * 即 {@link SocialUserDO} 与 UserDO 的关联表
 *
 * @author zhaoff
 */
@TableName(value = "system_social_user_bind", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialUserBindDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;

    /**
     * 关联的用户编号
     * <p>
     * 关联 UserDO 的编号
     */
    private Long userId;

    /**
     * 用户类型
     * <p>
     * 枚举 {@link UserTypeEnum}
     */
    private Integer userType;

    /**
     * 社交平台的用户编号
     * <p>
     * 关联 {@link SocialUserDO#getId()}
     */
    private Long socialUserId;

    /**
     * 社交平台的类型
     * <p>
     * 冗余 {@link SocialUserDO#getType()}
     */
    private Integer socialType;

}
