package com.ubax.governance.repo;

import com.ubax.governance.domain.ParsingRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParsingRuleRepository extends JpaRepository<ParsingRule, Long> {

    List<ParsingRule> findByTenantIdAndEnabledTrueOrderByPriorityAscIdAsc(Long tenantId);

    Page<ParsingRule> findByTenantId(Long tenantId, Pageable pageable);

    Page<ParsingRule> findByTenantIdAndAppId(Long tenantId, Long appId, Pageable pageable);
}
