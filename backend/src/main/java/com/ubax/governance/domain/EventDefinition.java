package com.ubax.governance.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Event dictionary entry: declares an allowed event key with a standard name,
 * type, property schema and validation policy. Scoped by tenant + app
 * (appId = 0 means the whole tenant).
 */
@Data
@Entity
@Table(name = "uba_event_def",
        uniqueConstraints = @UniqueConstraint(name = "uk_event_def", columnNames = {"tenant_id", "app_id", "event_key"}),
        indexes = @Index(name = "idx_event_def_tenant", columnList = "tenant_id"))
public class EventDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id")
    private Long tenantId;

    /** 0 = applies to every app of the tenant. */
    @Column(name = "app_id")
    private Long appId = 0L;

    @Column(name = "event_key", nullable = false, length = 120)
    private String eventKey;

    @Column(name = "event_name", length = 120)
    private String eventName;

    /** pageview | click | leave | custom */
    @Column(length = 32)
    private String type = "custom";

    /** JSON schema declaring property name/type/required. */
    @Lob
    @Column(name = "properties_schema", columnDefinition = "TEXT")
    private String propertiesSchema;

    /** JSON validation policy. */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String validate;

    /** What to do when an incoming event does not match any definition: MARK | DROP | UNKNOWN. */
    @Column(name = "on_miss", length = 16)
    private String onMiss = "MARK";

    private boolean enabled = true;

    private int version = 1;

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
