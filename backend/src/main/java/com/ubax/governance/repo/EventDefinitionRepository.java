package com.ubax.governance.repo;

import com.ubax.governance.domain.EventDefinition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventDefinitionRepository extends JpaRepository<EventDefinition, Long> {

    List<EventDefinition> findByTenantIdAndEnabledTrue(Long tenantId);

    Optional<EventDefinition> findByTenantIdAndAppIdAndEventKey(Long tenantId, Long appId, String eventKey);

    boolean existsByTenantIdAndAppIdAndEventKey(Long tenantId, Long appId, String eventKey);

    Page<EventDefinition> findByTenantId(Long tenantId, Pageable pageable);

    Page<EventDefinition> findByTenantIdAndAppId(Long tenantId, Long appId, Pageable pageable);
}
