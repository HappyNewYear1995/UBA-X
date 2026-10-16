package com.ubax.tenant.controller;

import com.ubax.common.ApiResponse;
import com.ubax.common.BusinessException;
import com.ubax.common.TenantContext;
import com.ubax.system.AuditService;
import com.ubax.tenant.domain.User;
import com.ubax.tenant.repo.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder, AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    private TenantContext.Principal principal() {
        TenantContext.Principal p = TenantContext.get();
        if (p == null) throw new BusinessException(401, "未认证");
        return p;
    }

    private boolean canManage() {
        TenantContext.Principal p = principal();
        return p.hasRole("SUPER_ADMIN") || p.hasRole("ADMIN");
    }

    /**
     * Load a user for a manage operation, enforcing tenant isolation: a tenant-scoped
     * principal (ADMIN, or SUPER_ADMIN bound to a tenant) may only touch users of the
     * same tenant; only a global SUPER_ADMIN (tenantId == null) may cross tenants.
     */
    private User loadManaged(Long id) {
        TenantContext.Principal p = principal();
        if (!canManage()) throw new BusinessException(403, "无权限");
        User u = userRepository.findById(id).orElseThrow(() -> new BusinessException(404, "用户不存在"));
        if (p.getTenantId() != null && !p.getTenantId().equals(u.getTenantId())) {
            throw new BusinessException(403, "无权操作其他租户的用户");
        }
        return u;
    }

    @GetMapping
    public ApiResponse<List<User>> list() {
        TenantContext.Principal p = principal();
        if (!canManage()) throw new BusinessException(403, "无权限");
        if (p.hasRole("SUPER_ADMIN") && p.getTenantId() == null) {
            return ApiResponse.ok(userRepository.findAll());
        }
        return ApiResponse.ok(userRepository.findByTenantId(p.getTenantId()));
    }

    @PostMapping
    public ApiResponse<User> create(@RequestBody Map<String, Object> body) {
        TenantContext.Principal p = principal();
        if (!canManage()) throw new BusinessException(403, "无权限");
        String username = (String) body.get("username");
        String password = (String) body.get("password");
        if (username == null || password == null) throw new BusinessException("用户名和密码必填");
        String role = body.getOrDefault("role", "VIEWER").toString();
        if (!p.hasRole("SUPER_ADMIN") && "SUPER_ADMIN".equals(role)) throw new BusinessException(403, "无权创建平台管理员");

        Long tenantId = p.getTenantId();
        if (p.hasRole("SUPER_ADMIN") && body.get("tenantId") != null) {
            tenantId = Long.valueOf(body.get("tenantId").toString());
        }
        if (userRepository.findByTenantIdAndUsername(tenantId, username).isPresent()) {
            throw new BusinessException("用户名已存在");
        }
        User u = new User();
        u.setTenantId(tenantId);
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(password));
        u.setNickname((String) body.getOrDefault("nickname", username));
        u.setEmail((String) body.get("email"));
        u.setRole(role);
        u.setStatus("ACTIVE");
        User saved = userRepository.save(u);
        auditService.record("CREATE", "用户管理", "新建用户", "用户名=" + username + ", 角色=" + role);
        return ApiResponse.ok(saved);
    }

    @PutMapping("/{id}")
    public ApiResponse<User> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        TenantContext.Principal p = principal();
        User u = loadManaged(id);
        if (body.get("nickname") != null) u.setNickname(body.get("nickname").toString());
        if (body.get("email") != null) u.setEmail(body.get("email").toString());
        if (body.get("role") != null) {
            String role = body.get("role").toString();
            if ("SUPER_ADMIN".equals(role) && !p.hasRole("SUPER_ADMIN")) {
                throw new BusinessException(403, "无权授予平台管理员角色");
            }
            u.setRole(role);
        }
        if (body.get("status") != null) u.setStatus(body.get("status").toString());
        User saved = userRepository.save(u);
        auditService.record("UPDATE", "用户管理", "编辑用户", "用户=" + u.getUsername());
        return ApiResponse.ok(saved);
    }

    @PostMapping("/{id}/reset-password")
    public ApiResponse<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        User u = loadManaged(id);
        String pw = body.getOrDefault("password", "ubax@123").toString();
        u.setPassword(passwordEncoder.encode(pw));
        userRepository.save(u);
        auditService.record("UPDATE", "用户管理", "重置密码", "用户=" + u.getUsername());
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        TenantContext.Principal p = principal();
        User u = loadManaged(id);
        if (p.getUserId().equals(id)) throw new BusinessException("不能删除自己");
        userRepository.deleteById(id);
        auditService.record("DELETE", "用户管理", "删除用户", "用户=" + u.getUsername());
        return ApiResponse.ok();
    }
}
