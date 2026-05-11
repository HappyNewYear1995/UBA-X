package com.huanniankj.module.infra.api.logger;

import com.huanniankj.framework.common.biz.infra.logger.ApiErrorLogCommonApi;
import com.huanniankj.framework.common.biz.infra.logger.dto.ApiErrorLogCreateReqDTO;
import com.huanniankj.module.infra.service.logger.ApiErrorLogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * API 访问日志的 API 接口
 *
 * @author zhaoff
 */
@Service
@Validated
public class ApiErrorLogApiImpl implements ApiErrorLogCommonApi {

    @Resource
    private ApiErrorLogService apiErrorLogService;

    @Override
    public void createApiErrorLog(ApiErrorLogCreateReqDTO createDTO) {
        apiErrorLogService.createApiErrorLog(createDTO);
    }

}
