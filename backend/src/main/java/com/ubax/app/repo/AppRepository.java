package com.ubax.app.repo;

import com.ubax.app.domain.App;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppRepository extends JpaRepository<App, Long> {
    Optional<App> findByAppKey(String appKey);
    List<App> findByTenantIdOrderByIdDesc(Long tenantId);
    List<App> findAllByOrderByIdDesc();
    long countByTenantId(Long tenantId);
    Optional<App> findByIdAndTenantId(Long id, Long tenantId);
}
