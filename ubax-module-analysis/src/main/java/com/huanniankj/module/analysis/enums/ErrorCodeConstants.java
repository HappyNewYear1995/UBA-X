package com.huanniankj.module.analysis.enums;

import com.huanniankj.framework.common.exception.ErrorCode;

/**
 * 分析模块错误码常量
 *
 * @author zhaoff
 */
public interface ErrorCodeConstants {

    ErrorCode FUNNEL_CONFIG_NOT_EXISTS = new ErrorCode(1_102_001_000, "漏斗分析配置不存在");
    ErrorCode RETENTION_CONFIG_NOT_EXISTS = new ErrorCode(1_102_002_000, "留存分析配置不存在");
    ErrorCode PATH_CONFIG_NOT_EXISTS = new ErrorCode(1_102_003_000, "路径分析配置不存在");

}
