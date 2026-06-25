package com.huanniankj.module.app.enums;

import com.huanniankj.framework.common.exception.ErrorCode;

/**
 * app 模块错误码枚举类
 * <p>
 * app 模块，使用 1-104-000-000 段
 *
 * @author zhaoff
 */
public interface ErrorCodeConstants {

    // ========== 告警规则模块 1-104-001-000 ==========
    ErrorCode ALERT_RULE_NOT_EXISTS = new ErrorCode(1_104_001_000, "告警规则不存在");
    ErrorCode ALERT_RULE_NAME_DUPLICATE = new ErrorCode(1_104_001_001, "告警规则名称已存在");

    // ========== 告警记录模块 1-104-002-000 ==========
    ErrorCode ALERT_RECORD_NOT_EXISTS = new ErrorCode(1_104_002_000, "告警记录不存在");

    // ========== 安全检测规则模块 1-104-003-000 ==========
    ErrorCode SECURITY_RULE_NOT_EXISTS = new ErrorCode(1_104_003_000, "安全检测规则不存在");
    ErrorCode SECURITY_RULE_NAME_DUPLICATE = new ErrorCode(1_104_003_001, "安全检测规则名称已存在");

    // ========== 安全检测事件模块 1-104-004-000 ==========
    ErrorCode SECURITY_EVENT_NOT_EXISTS = new ErrorCode(1_104_004_000, "安全检测事件不存在");

    // ========== 数据看板模块 1-104-005-000 ==========
    ErrorCode DASHBOARD_NOT_EXISTS = new ErrorCode(1_104_005_000, "数据看板不存在");
    ErrorCode DASHBOARD_NAME_DUPLICATE = new ErrorCode(1_104_005_001, "数据看板名称已存在");

}
