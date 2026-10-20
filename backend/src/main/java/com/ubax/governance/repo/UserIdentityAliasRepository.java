package com.ubax.governance.repo;

import com.ubax.governance.domain.UserIdentityAlias;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserIdentityAliasRepository extends JpaRepository<UserIdentityAlias, Long> {

    Optional<UserIdentityAlias> findByTenantIdAndAppIdAndDistinctId(Long tenantId, Long appId, String distinctId);

    Page<UserIdentityAlias> findByTenantId(Long tenantId, Pageable pageable);

    Page<UserIdentityAlias> findByTenantIdAndAppId(Long tenantId, Long appId, Pageable pageable);
}
