package com.huanniankj.module.system.dal.dataobject.logger;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.huanniankj.framework.common.enums.UserTypeEnum;
import com.huanniankj.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;

/**
 * 操作日志表
 *
 * @author zhaoff
 */
@TableName(value = "system_operate_log", autoResultMap = true)
@Data
public class OperateLogDO extends BaseDO {

    /**
     * 日志主键
     */
    @TableId
    private Long id;

    /**
     * 链路追踪编号
     * <p>
     * 一般来说，通过链路追踪编号，可以将访问日志，错误日志，链路追踪日志，logger 打印日志等，结合在一起，从而进行排错。
     */
    private String traceId;

    /**
     * 用户编号
     * <p>
     * 关联 MemberUserDO 的 id 属性，或者 AdminUserDO 的 id 属性
     */
    private Long userId;

    /**
     * 用户类型
     * <p>
     * 关联 {@link  UserTypeEnum}
     */
    private Integer userType;

    /**
     * 操作模块类型
     */
    private String type;

    /**
     * 操作名
     */
    private String subType;

    /**
     * 操作模块业务编号
     */
    private Long bizId;

    /**
     * 日志内容，记录整个操作的明细
     * <p>
     * 例如说，修改编号为 1 的用户信息，将性别从男改成女。
     */
    private String action;

    /**
     * 拓展字段，有些复杂的业务，需要记录一些字段 ( JSON 格式 )
     * <p>
     * 例如说，记录订单编号，{ orderId: "1"}
     */
    private String extra;

    /**
     * 请求方法名
     */
    private String requestMethod;

    /**
     * 请求地址
     */
    private String requestUrl;

    /**
     * 用户 IP
     */
    private String userIp;

    /**
     * 浏览器 UA
     */
    private String userAgent;

}
