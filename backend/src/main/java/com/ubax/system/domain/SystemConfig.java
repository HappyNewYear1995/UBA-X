package com.ubax.system.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/** Global key-value system parameter (platform-wide). */
@Data
@Entity
@Table(name = "uba_sys_config")
public class SystemConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "config_key", nullable = false, unique = true, length = 80)
    private String configKey;

    @Column(name = "config_name", nullable = false, length = 80)
    private String configName;

    @Column(name = "config_value", length = 500)
    private String configValue;

    /** STRING / NUMBER / BOOLEAN */
    @Column(name = "value_type", length = 16)
    private String valueType = "STRING";

    @Column(name = "group_key", length = 40)
    private String groupKey;

    @Column(length = 200)
    private String remark;

    @Column(nullable = false)
    private boolean builtin = true;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = LocalDateTime.now();
    }
}
