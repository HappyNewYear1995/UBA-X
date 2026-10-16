package com.ubax.tenant.domain;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Role catalog describing permission sets. Permissions are stored as a comma-separated
 * string of permission codes; the frontend uses these to control menu/button visibility.
 */
@Data
@Entity
@Table(name = "uba_role")
public class Role {
    @Id
    @Column(length = 20)
    private String code;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String permissions;
}
