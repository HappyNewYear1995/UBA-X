package com.huanniankj.module.system.api.tenant;

import com.huanniankj.framework.common.biz.system.tenant.TenantCommonApi;
import com.huanniankj.module.system.service.tenant.TenantService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 多租户的 API 实现类
 *
 * @author zhaoff
 */
@Service
public class TenantApiImpl implements TenantCommonApi {

    @Resource
    private TenantService tenantService;

    @Override
    public List<Long> getTenantIdList() {
        return tenantService.getTenantIdList();
    }

    @Override
    public void validateTenant(Long id) {
        tenantService.validTenant(id);
    }

}
