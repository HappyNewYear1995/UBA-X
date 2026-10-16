package com.ubax.tenant.repo;

import com.ubax.tenant.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByTenantIdAndUsername(Long tenantId, String username);
    Optional<User> findByUsernameAndTenantIdIsNull(String username);
    List<User> findByTenantId(Long tenantId);
    long countByTenantId(Long tenantId);
    long countByRole(String role);
}
