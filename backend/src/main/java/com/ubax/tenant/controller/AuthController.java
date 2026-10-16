package com.ubax.tenant.controller;

import com.ubax.common.ApiResponse;
import com.ubax.common.BusinessException;
import com.ubax.common.TenantContext;
import com.ubax.system.AuditService;
import com.ubax.system.OnlineUserRegistry;
import com.ubax.tenant.domain.User;
import com.ubax.tenant.dto.LoginRequest;
import com.ubax.tenant.dto.LoginResponse;
import com.ubax.tenant.repo.UserRepository;
import com.ubax.tenant.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final OnlineUserRegistry onlineUserRegistry;

    public AuthController(AuthService authService, UserRepository userRepository,
                          AuditService auditService, OnlineUserRegistry onlineUserRegistry) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.onlineUserRegistry = onlineUserRegistry;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.ok(authService.login(req));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        TenantContext.Principal p = TenantContext.get();
        if (p != null) {
            onlineUserRegistry.leave(p.getUserId());
            auditService.recordAuth("LOGOUT", p.getTenantId(), p.getUserId(), p.getUsername(), true, "退出登录");
        }
        return ApiResponse.ok();
    }

    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> me() {
        TenantContext.Principal p = TenantContext.get();
        if (p == null) throw new BusinessException(401, "未认证");
        User u = userRepository.findById(p.getUserId()).orElseThrow(() -> new BusinessException(401, "用户不存在"));
        Map<String, Object> data = new HashMap<>();
        data.put("userId", u.getId());
        data.put("username", u.getUsername());
        data.put("nickname", u.getNickname());
        data.put("role", u.getRole());
        data.put("tenantId", u.getTenantId());
        data.put("permissions", authService.permissionsFor(u.getRole()));
        return ApiResponse.ok(data);
    }
}
