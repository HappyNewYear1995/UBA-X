package com.ubax.tenant.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "uba_tenant")
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /** ACTIVE / DISABLED */
    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    /** FREE / STANDARD / ENTERPRISE */
    @Column(length = 20)
    private String plan = "STANDARD";

    @Column(length = 255)
    private String contact;

    private LocalDateTime createdAt;
    private LocalDateTime expireAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
