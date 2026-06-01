package com.huanniankj.module.gather.enums;

import com.huanniankj.framework.common.exception.ErrorCode;

/**
 * gather 错误码枚举类
 * <p>
 * gather 模块，使用 1-101-000-000 段
 *
 * @author zhaoff
 */
public interface ErrorCodeConstants {

    // ========== Agent 模块 1-101-000-000 ==========
    ErrorCode AGENT_NOT_EXISTS = new ErrorCode(1_101_000_000, "Agent 不存在");
    ErrorCode AGENT_OFFLINE = new ErrorCode(1_101_000_001, "Agent 不在线");
    ErrorCode AGENT_PUSH_COMMAND_FAILED = new ErrorCode(1_101_000_002, "推送命令失败");
    ErrorCode AGENT_PUSH_CONFIG_FAILED = new ErrorCode(1_101_000_003, "推送配置失败");

    // ========== 事件管理模块 1-101-001-000 ==========

    ErrorCode EVENT_CONFIG_NOT_EXISTS = new ErrorCode(1_101_001_000, "事件配置不存在");
    ErrorCode EVENT_NOT_EXISTS = new ErrorCode(1_101_001_001, "事件不存在");

    // ========== 运行监控模块 1-101-002-000 ==========
    ErrorCode MONITOR_METRIC_NOT_EXISTS = new ErrorCode(1_101_002_000, "监控指标不存在");

}
