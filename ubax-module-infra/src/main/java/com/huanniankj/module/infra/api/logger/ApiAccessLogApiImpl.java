package com.huanniankj.module.infra.api.logger;

import com.huanniankj.framework.common.biz.infra.logger.ApiAccessLogCommonApi;
import com.huanniankj.framework.common.biz.infra.logger.dto.ApiAccessLogCreateReqDTO;
import com.huanniankj.module.infra.service.logger.ApiAccessLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * API 访问日志的 API 实现类
 *
 * @author zhaoff
 */
@Service
@Validated
public class ApiAccessLogApiImpl implements ApiAccessLogCommonApi {

    @Autowired
    private ApiAccessLogService apiAccessLogService;

    @Override
    public void createApiAccessLog(ApiAccessLogCreateReqDTO createDTO) {
        apiAccessLogService.createApiAccessLog(createDTO);
    }

}
