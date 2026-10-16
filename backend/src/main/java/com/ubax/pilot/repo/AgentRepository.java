package com.ubax.pilot.repo;

import com.ubax.pilot.domain.Agent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AgentRepository extends JpaRepository<Agent, Long> {
    Optional<Agent> findByAgentKey(String agentKey);
    List<Agent> findByTenantIdOrderByIdDesc(Long tenantId);
    Optional<Agent> findByIdAndTenantId(Long id, Long tenantId);
    long countByTenantId(Long tenantId);
    long countByTenantIdAndStatus(Long tenantId, String status);
    List<Agent> findByStatusAndLastHeartbeatBefore(String status, LocalDateTime cutoff);
}
