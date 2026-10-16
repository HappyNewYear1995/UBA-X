package com.ubax.tenant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private Long userId;
    private String username;
    private String nickname;
    private String role;
    private Long tenantId;
    private String tenantName;
    private List<String> permissions;
}
