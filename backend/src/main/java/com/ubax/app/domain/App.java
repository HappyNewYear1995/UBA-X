package com.ubax.app.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "uba_app")
public class App {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 100)
    private String name;

    /** Public SDK ingestion key. */
    @Column(name = "app_key", nullable = false, unique = true, length = 64)
    private String appKey;

    /** WEB / H5 / APP / MINIPROGRAM */
    @Column(nullable = false, length = 20)
    private String platform = "WEB";

    @Column(length = 255)
    private String description;

    /** auto pageview / click / heatmap tracking */
    @Column(name = "auto_track")
    private Boolean autoTrack = true;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
