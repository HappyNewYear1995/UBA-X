package com.ubax.governance.repo;

import com.ubax.governance.domain.IdentityRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IdentityRuleRepository extends JpaRepository<IdentityRule, Long> {

    List<IdentityRule> findByTenantId(Long tenantId);

    Optional<IdentityRule> findByTenantIdAndAppIdAndEnabledTrue(Long tenantId, Long appId);

    Optional<IdentityRule> findFirstByTenantIdAndEnabledTrueOrderByAppIdDesc(Long tenantId);
}
