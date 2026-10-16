package com.ubax.system.repo;

import com.ubax.system.domain.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {
    Optional<SystemConfig> findByConfigKey(String configKey);
    List<SystemConfig> findAllByOrderByGroupKeyAscIdAsc();
}
