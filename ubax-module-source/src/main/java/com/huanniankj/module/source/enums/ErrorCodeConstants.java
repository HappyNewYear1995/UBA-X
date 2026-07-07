package com.huanniankj.module.source.enums;

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

    // ========== 数据源管理模块 1-101-003-000 ==========
    ErrorCode SOURCE_DATASOURCE_NOT_EXISTS = new ErrorCode(1_101_003_000, "数据库数据源不存在");
    ErrorCode SOURCE_DATASOURCE_NAME_DUPLICATE = new ErrorCode(1_101_003_001, "数据库数据源名称已存在");
    ErrorCode DATASOURCE_CONNECTION_FAILED = new ErrorCode(1_101_003_002, "数据源主机地址和端口错误");
    ErrorCode DATASOURCE_SQL_SECURITY_VIOLATION = new ErrorCode(1_101_003_003, "SQL 安全校验失败，禁止执行高危操作");
    ErrorCode DATASOURCE_SQL_EXECUTION_FAILED = new ErrorCode(1_101_003_004, "SQL 执行失败");
    ErrorCode DATASOURCE_NOT_INITIALIZED = new ErrorCode(1_101_003_005, "数据源未初始化");

    // ========== WebService 数据源管理模块 1-101-006-000 ==========
    ErrorCode WEBSERVICE_DATASOURCE_NOT_EXISTS = new ErrorCode(1_101_006_000, "WebService 数据源不存在");
    ErrorCode WEBSERVICE_DATASOURCE_NAME_DUPLICATE = new ErrorCode(1_101_006_001, "WebService 数据源名称已存在");
    ErrorCode WEBSERVICE_URL_INVALID = new ErrorCode(1_101_006_002, "WebService 地址格式无效");

    // ========== 处理脚本 1_101_005_000 ==========
    ErrorCode PROCESSING_SCRIPT_NOT_EXISTS = new ErrorCode(1_101_005_000, "处理脚本不存在");
    ErrorCode PROCESSING_SCRIPT_CODE_DUPLICATE = new ErrorCode(1_101_005_001, "处理脚本编码已存在");
    ErrorCode PROCESSING_SCRIPT_LOG_NOT_EXISTS = new ErrorCode(1_101_005_002, "处理脚本执行日志不存在");

}
