package com.huanniankj.module.processing.enums;

import com.huanniankj.framework.common.exception.ErrorCode;

/**
 * 数据处理模块错误码常量
 * <p>
 * collect 模块，使用 1-103-000-000 段
 *
 * @author zhaoff
 */
public interface ErrorCodeConstants {

    // ========== 事件配置模块 1-103-001-000 ==========
    ErrorCode EVENT_CONFIG_NOT_EXISTS = new ErrorCode(1_103_001_000, "事件配置不存在");
    ErrorCode EVENT_CONFIG_CODE_DUPLICATE = new ErrorCode(1_103_001_001, "事件配置编码已存在");

    // ========== 清洗管道模块 1-103-002-000 ==========
    ErrorCode CLEAN_PIPELINE_NOT_EXISTS = new ErrorCode(1_103_002_000, "清洗管道不存在");
    ErrorCode CLEAN_PIPELINE_NAME_DUPLICATE = new ErrorCode(1_103_002_001, "清洗管道名称已存在");

    // ========== 异常日志模块 1-103-003-000 ==========
    ErrorCode ERROR_LOG_NOT_EXISTS = new ErrorCode(1_103_003_000, "异常日志不存在");

    // ========== 数据日志模块 1-103-004-000 ==========
    ErrorCode DATA_LOG_NOT_EXISTS = new ErrorCode(1_103_004_000, "数据日志不存在");

    // ========== 事件模块 1-103-005-000 ==========
    ErrorCode EVENT_NOT_EXISTS = new ErrorCode(1_103_005_000, "事件不存在");

}
