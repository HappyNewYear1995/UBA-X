package com.ubax.pilot.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * A distributed collection probe ("agent") managed by ubax-pilot.
 */
@Data
@Entity
@Table(name = "uba_agent",
        indexes = @Index(name = "idx_agent_tenant", columnList = "tenant_id,status"))
public class Agent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 100)
    private String name;

    /** Secret token the probe uses to authenticate register/heartbeat. */
    @Column(name = "agent_key", nullable = false, unique = true, length = 64)
    private String agentKey;

    @Column(length = 200)
    private String host;
    @Column(length = 64)
    private String ip;
    @Column(length = 32)
    private String version;
    @Column(length = 32)
    private String os;
    @Column(length = 32)
    private String arch;

    /** INSTALLING | ONLINE | OFFLINE */
    @Column(nullable = false, length = 20)
    private String status = "INSTALLING";

    @Column(length = 255)
    private String tags;

    /** Effective configuration pushed to the agent (JSON). */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String config;

    /** Config version, bumped on remote update so agents can detect changes. */
    @Column(name = "config_version")
    private Long configVersion = 0L;

    /** Last reported metrics snapshot (JSON). */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String metrics;

    @Column(name = "events_sent")
    private Long eventsSent = 0L;

    @Column(name = "last_heartbeat")
    private LocalDateTime lastHeartbeat;

    @Column(name = "registered_at")
    private LocalDateTime registeredAt;

    @PrePersist
    void onCreate() {
        if (registeredAt == null) registeredAt = LocalDateTime.now();
    }
}
