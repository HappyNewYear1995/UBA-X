package com.ubax.system.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Unified audit trail. Covers both login events (type LOGIN/LOGOUT) and
 * business operations (CREATE/UPDATE/DELETE/GRANT/OTHER). Login-log and
 * operation-log pages are just different filters over this one table.
 */
@Data
@Entity
@Table(name = "uba_audit_log", indexes = {
        @Index(name = "idx_audit_tenant", columnList = "tenant_id"),
        @Index(name = "idx_audit_type", columnList = "type"),
        @Index(name = "idx_audit_created", columnList = "created_at")
})
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "user_id")
    private Long userId;

    @Column(length = 50)
    private String username;

    /** LOGIN / LOGOUT / CREATE / UPDATE / DELETE / GRANT / OTHER */
    @Column(nullable = false, length = 20)
    private String type = "OTHER";

    @Column(length = 40)
    private String module;

    @Column(length = 120)
    private String action;

    @Column(length = 600)
    private String detail;

    @Column(length = 60)
    private String ip;

    @Column(length = 80)
    private String location;

    @Column(name = "user_agent", length = 300)
    private String userAgent;

    @Column(nullable = false)
    private boolean success = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
