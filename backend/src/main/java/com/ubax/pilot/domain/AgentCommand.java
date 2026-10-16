package com.ubax.pilot.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * A remote command queued for an agent. Agents pull pending commands on heartbeat.
 */
@Data
@Entity
@Table(name = "uba_agent_command", indexes = @Index(name = "idx_cmd_agent", columnList = "agent_id,status"))
public class AgentCommand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "agent_id", nullable = false)
    private Long agentId;

    /** RELOAD_CONFIG | START | STOP | RESTART | FLUSH | UPGRADE */
    @Column(nullable = false, length = 32)
    private String type;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String payload;

    /** PENDING | DELIVERED | DONE | FAILED */
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Lob
    @Column(columnDefinition = "TEXT")
    private String result;

    private LocalDateTime createdAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime finishedAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
