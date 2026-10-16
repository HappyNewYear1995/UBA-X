package com.ubax.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    /** Optional tenant code; super admin may omit. */
    private String tenantCode;
}
