package com.ubax.app;

import com.ubax.app.domain.App;
import com.ubax.app.repo.AppRepository;
import com.ubax.common.BusinessException;
import com.ubax.common.TenantContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AppService {

    private final AppRepository appRepository;

    public AppService(AppRepository appRepository) {
        this.appRepository = appRepository;
    }

    public List<App> listForCurrentTenant() {
        TenantContext.Principal p = TenantContext.get();
        if (p == null) throw new BusinessException(401, "未认证");
        // Platform super admins (tenantId == null) see every tenant's apps.
        if (p.hasRole("SUPER_ADMIN") && p.getTenantId() == null) {
            return appRepository.findAllByOrderByIdDesc();
        }
        return appRepository.findByTenantIdOrderByIdDesc(p.getTenantId());
    }

    public App create(App in) {
        Long tid = TenantContext.requireTenantId();
        App a = new App();
        a.setTenantId(tid);
        a.setName(in.getName());
        a.setPlatform(in.getPlatform() == null ? "WEB" : in.getPlatform());
        a.setDescription(in.getDescription());
        a.setAutoTrack(in.getAutoTrack() == null || in.getAutoTrack());
        a.setAppKey("ubax_" + UUID.randomUUID().toString().replace("-", "").substring(0, 24));
        a.setStatus("ACTIVE");
        return appRepository.save(a);
    }

    public App update(Long id, App in) {
        App a = get(id);
        if (in.getName() != null) a.setName(in.getName());
        if (in.getPlatform() != null) a.setPlatform(in.getPlatform());
        if (in.getDescription() != null) a.setDescription(in.getDescription());
        if (in.getAutoTrack() != null) a.setAutoTrack(in.getAutoTrack());
        if (in.getStatus() != null) a.setStatus(in.getStatus());
        return appRepository.save(a);
    }

    public App get(Long id) {
        TenantContext.Principal p = TenantContext.get();
        if (p == null) throw new BusinessException(401, "未认证");
        // Global super admins (tenantId == null) may access any tenant's app; scoping by
        // requireTenantId() here would throw a body-401 and silently kick them to login.
        if (p.hasRole("SUPER_ADMIN") && p.getTenantId() == null) {
            return appRepository.findById(id).orElseThrow(() -> new BusinessException(404, "应用不存在"));
        }
        return appRepository.findByIdAndTenantId(id, p.getTenantId())
                .orElseThrow(() -> new BusinessException(404, "应用不存在"));
    }

    public void delete(Long id) {
        App a = get(id);
        appRepository.delete(a);
    }

    /** Regenerate the ingestion key for an app. */
    public App rotateKey(Long id) {
        App a = get(id);
        a.setAppKey("ubax_" + UUID.randomUUID().toString().replace("-", "").substring(0, 24));
        return appRepository.save(a);
    }
}
