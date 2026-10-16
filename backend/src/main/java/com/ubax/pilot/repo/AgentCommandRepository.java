package com.ubax.pilot.repo;

import com.ubax.pilot.domain.AgentCommand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgentCommandRepository extends JpaRepository<AgentCommand, Long> {
    List<AgentCommand> findByAgentIdAndStatusOrderByIdAsc(Long agentId, String status);
    List<AgentCommand> findByTenantIdOrderByIdDesc(Long tenantId);
    List<AgentCommand> findByAgentIdAndTenantIdOrderByIdDesc(Long agentId, Long tenantId);
}
