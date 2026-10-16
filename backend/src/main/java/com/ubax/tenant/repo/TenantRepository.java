package com.ubax.tenant.repo;

import com.ubax.tenant.domain.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
    Optional<Tenant> findByCode(String code);
    boolean existsByCode(String code);
    List<Tenant> findAllByOrderByIdAsc();
}
