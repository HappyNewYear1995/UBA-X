package com.ubax.tenant.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "uba_user", uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "username"}))
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(nullable = false, length = 50)
    private String username;

    @JsonIgnore
    @Column(nullable = false, length = 100)
    private String password;

    @Column(length = 50)
    private String nickname;

    @Column(length = 100)
    private String email;

    /** SUPER_ADMIN / ADMIN / ANALYST / VIEWER */
    @Column(nullable = false, length = 20)
    private String role = "VIEWER";

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
