package com.ubax.system.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/** System notice / announcement broadcast to users via the header bell. */
@Data
@Entity
@Table(name = "uba_sys_notice")
public class Notice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(length = 2000)
    private String content;

    /** NOTICE (通知) / ANNOUNCE (公告) */
    @Column(length = 16)
    private String type = "NOTICE";

    /** DRAFT / PUBLISHED */
    @Column(length = 16)
    private String status = "DRAFT";

    @Column(length = 50)
    private String publisher;

    @Column(name = "is_top")
    private boolean top = false;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
