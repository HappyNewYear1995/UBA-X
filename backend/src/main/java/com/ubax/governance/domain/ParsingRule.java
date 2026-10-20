package com.ubax.governance.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Parsing rule applied to an incoming event before persistence. Rules are
 * ordered by priority (ascending) within a tenant + app scope and may set /
 * rename / extract fields, drop the event, or anonymize values.
 */
@Data
@Entity
@Table(name = "uba_parsing_rule",
        indexes = @Index(name = "idx_parsing_rule_tenant", columnList = "tenant_id"))
public class ParsingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id")
    private Long tenantId;

    /** 0 = applies to every app of the tenant. */
    @Column(name = "app_id")
    private Long appId = 0L;

    /** Lower value runs first. */
    private int priority = 100;

    @Column(length = 120)
    private String name;

    /** JSON match conditions (AND semantics): event/type/path/app/... */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String matchExpr;

    /** JSON action: set | rename | extract | drop | anonymize. */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String action;

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
