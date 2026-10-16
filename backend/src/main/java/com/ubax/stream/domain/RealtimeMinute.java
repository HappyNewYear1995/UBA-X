package com.ubax.stream.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Per-minute realtime rollup produced by the in-app stream engine. Powers the
 * live/realtime dashboard without scanning the raw event table.
 */
@Data
@Entity
@Table(name = "uba_realtime_minute",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "app_id", "minute"}),
        indexes = @Index(name = "idx_rt_tenant_minute", columnList = "tenant_id,minute"))
public class RealtimeMinute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    /** 0 when aggregating across all apps of the tenant. */
    @Column(name = "app_id", nullable = false)
    private Long appId = 0L;

    @Column(name = "minute", nullable = false)
    private LocalDateTime minute;

    private Long pv = 0L;
    private Long uv = 0L;
    private Long events = 0L;

    @PrePersist
    void onCreate() {
        if (appId == null) appId = 0L;
    }
}
