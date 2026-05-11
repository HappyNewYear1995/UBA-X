package com.huanniankj.module.system.dal.dataobject.tenant;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.huanniankj.framework.common.enums.CommonStatusEnum;
import com.huanniankj.framework.mybatis.core.dataobject.BaseDO;
import com.huanniankj.framework.mybatis.core.type.StringListTypeHandler;
import com.huanniankj.framework.tenant.core.aop.TenantIgnore;
import com.huanniankj.module.system.dal.dataobject.user.AdminUserDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 租户 DO
 *
 * @author zhaoff
 */
@TableName(value = "system_tenant", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TenantIgnore
public class TenantDO extends BaseDO {

    /**
     * 套餐编号 - 系统
     */
    public static final Long PACKAGE_ID_SYSTEM = 0L;

    /**
     * 租户编号，自增
     */
    private Long id;

    /**
     * 租户名，唯一
     */
    private String name;

    /**
     * 联系人的用户编号
     * <p>
     * 关联 {@link AdminUserDO#getId()}
     */
    private Long contactUserId;

    /**
     * 联系人
     */
    private String contactName;

    /**
     * 联系手机
     */
    private String contactMobile;

    /**
     * 租户状态
     * <p>
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

    /**
     * 绑定域名列表
     * <p>
     * 1. 考虑到对微信小程序的兼容，也允许传递 appid
     * 2. 为什么是数组，考虑到管理后台、会员前台都有独立的域名，又或者多个管理后台
     */
    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> websites;

    /**
     * 租户套餐编号
     * <p>
     * 关联 {@link TenantPackageDO#getId()}
     * 特殊逻辑：系统内置租户，不使用套餐，暂时使用 {@link #PACKAGE_ID_SYSTEM} 标识
     */
    private Long packageId;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 账号数量
     */
    private Integer accountCount;

}
