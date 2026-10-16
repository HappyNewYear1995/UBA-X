package com.ubax.profile.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "uba_user_profile",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "distinct_id"}),
        indexes = @Index(name = "idx_profile_tenant", columnList = "tenant_id,last_seen"))
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "app_id")
    private Long appId;

    @Column(name = "distinct_id", nullable = false, length = 64)
    private String distinctId;

    @Column(name = "user_id", length = 64)
    private String userId;

    @Column(name = "first_seen")
    private LocalDateTime firstSeen;
    @Column(name = "last_seen")
    private LocalDateTime lastSeen;

    @Column(name = "visit_count")
    private Long visitCount = 0L;
    @Column(name = "event_count")
    private Long eventCount = 0L;
    @Column(name = "pageview_count")
    private Long pageviewCount = 0L;

    @Column(name = "last_url", length = 1000)
    private String lastUrl;

    @Column(length = 32)
    private String device;
    @Column(length = 32)
    private String os;
    @Column(length = 32)
    private String browser;
    @Column(length = 64)
    private String region;
    @Column(length = 64)
    private String city;

    /** computed segment labels, comma separated */
    @Column(length = 255)
    private String tags;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String properties;

    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
