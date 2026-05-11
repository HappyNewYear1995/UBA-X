package com.huanniankj.module.system.dal.dataobject.logger;

import com.baomidou.mybatisplus.annotation.TableName;
import com.huanniankj.framework.common.enums.UserTypeEnum;
import com.huanniankj.framework.mybatis.core.dataobject.BaseDO;
import com.huanniankj.module.system.enums.logger.LoginLogTypeEnum;
import com.huanniankj.module.system.enums.logger.LoginResultEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 登录日志表
 * <p>
 * 注意，包括登录和登出两种行为
 *
 * @author zhaoff
 */
@TableName("system_login_log")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class LoginLogDO extends BaseDO {

    /**
     * 日志主键
     */
    private Long id;

    /**
     * 日志类型
     * <p>
     * 枚举 {@link LoginLogTypeEnum}
     */
    private Integer logType;

    /**
     * 链路追踪编号
     */
    private String traceId;

    /**
     * 用户编号
     */
    private Long userId;

    /**
     * 用户类型
     * <p>
     * 枚举 {@link UserTypeEnum}
     */
    private Integer userType;

    /**
     * 用户账号
     * <p>
     * 冗余，因为账号可以变更
     */
    private String username;

    /**
     * 登录结果
     * <p>
     * 枚举 {@link LoginResultEnum}
     */
    private Integer result;

    /**
     * 用户 IP
     */
    private String userIp;

    /**
     * 浏览器 UA
     */
    private String userAgent;

}
