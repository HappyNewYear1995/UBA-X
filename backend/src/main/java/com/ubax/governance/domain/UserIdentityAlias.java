package com.ubax.governance.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Maps an anonymous distinctId to a unified (usually logged-in) identity so
 * that pre- and post-login events fold into a single user profile.
 */
@Data
@Entity
@Table(name = "uba_user_alias",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_alias", columnNames = {"tenant_id", "app_id", "distinct_id"}),
        indexes = @Index(name = "idx_user_alias_tenant", columnList = "tenant_id"))
public class UserIdentityAlias {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "app_id")
    private Long appId = 0L;

    @Column(name = "distinct_id", nullable = false, length = 160)
    private String distinctId;

    @Column(name = "unified_id", nullable = false, length = 160)
    private String unifiedId;

    /** identify | auto */
    @Column(length = 24)
    private String source = "identify";

    @Column(name = "merged_at")
    private LocalDateTime mergedAt;

    @PrePersist
    void onCreate() {
        if (mergedAt == null) mergedAt = LocalDateTime.now();
    }
}
