package com.ubax.tenant.repo;

import com.ubax.tenant.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, String> {
}
