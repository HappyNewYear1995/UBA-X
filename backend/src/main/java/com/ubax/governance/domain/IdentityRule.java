package com.ubax.governance.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Identity-resolution strategy for a tenant + app scope. Decides how an
 * incoming (distinctId, userId) pair maps to the persisted profile key when no
 * explicit alias exists.
 */
@Data
@Entity
@Table(name = "uba_identity_rule",
        indexes = @Index(name = "idx_identity_rule_tenant", columnList = "tenant_id"))
public class IdentityRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id")
    private Long tenantId;

    /** 0 = applies to every app of the tenant. */
    @Column(name = "app_id")
    private Long appId = 0L;

    /** DISTINCT_FIRST | USER_FIRST | EXPLICIT. */
    @Column(length = 24)
    private String strategy = "DISTINCT_FIRST";

    private boolean enabled = true;

    @Column(length = 200)
    private String remark;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
