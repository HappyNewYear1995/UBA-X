package com.ubax.system;

import com.ubax.common.ApiResponse;
import com.ubax.common.BusinessException;
import com.ubax.common.TenantContext;
import com.ubax.tenant.domain.User;
import com.ubax.tenant.repo.TenantRepository;
import com.ubax.tenant.repo.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/** Current-user self-service: profile view/edit, password change, own login history. */
@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public AccountController(UserRepository userRepository, TenantRepository tenantRepository,
                             PasswordEncoder passwordEncoder, AuditService auditService) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    private User me() {
        TenantContext.Principal p = TenantContext.get();
        if (p == null) throw new BusinessException(401, "未认证");
        return userRepository.findById(p.getUserId()).orElseThrow(() -> new BusinessException(404, "用户不存在"));
    }

    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> me_info() {
        User u = me();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("userId", u.getId());
        m.put("username", u.getUsername());
        m.put("nickname", u.getNickname());
        m.put("email", u.getEmail());
        m.put("role", u.getRole());
        m.put("status", u.getStatus());
        m.put("tenantId", u.getTenantId());
        m.put("tenantName", u.getTenantId() == null ? "平台"
                : tenantRepository.findById(u.getTenantId()).map(t -> t.getName()).orElse("—"));
        m.put("createdAt", u.getCreatedAt());
        m.put("lastLoginAt", u.getLastLoginAt());
        return ApiResponse.ok(m);
    }

    @PutMapping("/profile")
    public ApiResponse<Map<String, Object>> updateProfile(@RequestBody Map<String, Object> body) {
        User u = me();
        if (body.get("nickname") != null) u.setNickname(body.get("nickname").toString());
        if (body.get("email") != null) u.setEmail(body.get("email").toString());
        userRepository.save(u);
        auditService.record("UPDATE", "个人中心", "修改资料", "用户=" + u.getUsername());
        return me_info();
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@RequestBody Map<String, Object> body) {
        User u = me();
        String oldPw = body.getOrDefault("oldPassword", "").toString();
        String newPw = body.getOrDefault("newPassword", "").toString();
        if (newPw.length() < 6) throw new BusinessException("新密码至少 6 位");
        if (!passwordEncoder.matches(oldPw, u.getPassword())) throw new BusinessException("原密码不正确");
        u.setPassword(passwordEncoder.encode(newPw));
        userRepository.save(u);
        auditService.record("UPDATE", "个人中心", "修改密码", "用户=" + u.getUsername());
        return ApiResponse.ok();
    }

    @GetMapping("/logins")
    public ApiResponse<Map<String, Object>> myLogins(@RequestParam(defaultValue = "20") int limit) {
        User u = me();
        return ApiResponse.ok(auditService.myLogins(u.getId(), Math.max(1, Math.min(limit, 100))));
    }
}
