package com.huanniankj.module.system.api.logger;

import com.huanniankj.module.system.api.logger.dto.LoginLogCreateReqDTO;
import jakarta.validation.Valid;

/**
 * 登录日志的 API 接口
 *
 * @author zhaoff
 */
public interface LoginLogApi {

    /**
     * 创建登录日志
     *
     * @param reqDTO 日志信息
     */
    void createLoginLog(@Valid LoginLogCreateReqDTO reqDTO);

}
