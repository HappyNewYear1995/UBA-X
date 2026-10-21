package com.ubax.event.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Raw immutable behavior event. Every collected hit (pageview, click, custom event,
 * heatmap point, replay frame marker) lands here and is consumed by the stream engine
 * to build aggregate rollups.
 */
@Data
@Entity
@Table(name = "uba_event", indexes = {
        @Index(name = "idx_event_tenant_time", columnList = "tenant_id,event_time"),
        @Index(name = "idx_event_app_time", columnList = "app_id,event_time"),
        @Index(name = "idx_event_distinct", columnList = "tenant_id,distinct_id"),
        @Index(name = "idx_event_session", columnList = "tenant_id,session_id"),
        @Index(name = "idx_event_name", columnList = "tenant_id,event_name"),
        @Index(name = "idx_event_type", columnList = "tenant_id,event_type")
})
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "app_id")
    private Long appId;

    /** pageview | click | event | custom | leave */
    @Column(name = "event_type", nullable = false, length = 20)
    private String eventType;

    @Column(name = "event_name", length = 100)
    private String eventName;

    @Column(name = "distinct_id", length = 64)
    private String distinctId;

    @Column(name = "user_id", length = 64)
    private String userId;

    @Column(name = "session_id", length = 64)
    private String sessionId;

    @Column(length = 1000)
    private String url;

    @Column(name = "url_path", length = 500)
    private String urlPath;

    @Column(length = 500)
    private String referrer;

    @Column(length = 300)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String properties;

    @Column(length = 64)
    private String ip;
    @Column(length = 64)
    private String country;
    @Column(length = 64)
    private String region;
    @Column(length = 64)
    private String city;

    @Column(name = "user_agent", length = 500)
    private String userAgent;
    @Column(length = 32)
    private String device;
    @Column(length = 32)
    private String os;
    @Column(length = 32)
    private String browser;
    @Column(length = 32)
    private String screen;

    /** click coordinates for heatmap */
    private Double x;
    private Double y;
    /** page geometry at click time (SDK >= 1.2.0); enables full-page projection */
    @Column(name = "scroll_x")
    private Integer scrollX;
    @Column(name = "scroll_y")
    private Integer scrollY;
    @Column(name = "vp_w")
    private Integer vpW;
    @Column(name = "vp_h")
    private Integer vpH;
    @Column(name = "doc_w")
    private Integer docW;
    @Column(name = "doc_h")
    private Integer docH;
    /** clicked element selector/text for heatmap + replay */
    @Column(length = 300)
    private String element;
    /** dwell/duration in ms for leave events */
    private Long duration;

    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;

    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (eventTime == null) eventTime = LocalDateTime.now();
    }
}
