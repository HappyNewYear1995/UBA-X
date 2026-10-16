package com.ubax.tenant.controller;

import com.ubax.app.repo.AppRepository;
import com.ubax.common.ApiResponse;
import com.ubax.common.BusinessException;
import com.ubax.common.TenantContext;
import com.ubax.event.repo.EventRepository;
import com.ubax.tenant.domain.Tenant;
import com.ubax.tenant.domain.User;
import com.ubax.tenant.repo.TenantRepository;
import com.ubax.tenant.repo.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final AppRepository appRepository;
    private final EventRepository eventRepository;
    private final PasswordEncoder passwordEncoder;

    public TenantController(TenantRepository tenantRepository, UserRepository userRepository,
                            AppRepository appRepository, EventRepository eventRepository,
                            PasswordEncoder passwordEncoder) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.appRepository = appRepository;
        this.eventRepository = eventRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private void requireSuperAdmin() {
        TenantContext.Principal p = TenantContext.get();
        if (p == null) throw new BusinessException(401, "未认证");
        if (!p.hasRole("SUPER_ADMIN")) throw new BusinessException(403, "仅平台管理员可操作");
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list() {
        requireSuperAdmin();
        List<Map<String, Object>> out = new ArrayList<>();
        for (Tenant t : tenantRepository.findAllByOrderByIdAsc()) {
            out.add(toView(t));
        }
        return ApiResponse.ok(out);
    }

    @GetMapping("/current")
    public ApiResponse<Tenant> current() {
        TenantContext.Principal p = TenantContext.get();
        if (p == null) throw new BusinessException(401, "未认证");
        // Platform super admins have no single tenant; return null instead of a body-401
        // (which the frontend interceptor would treat as auth failure and clear the token).
        if (p.getTenantId() == null) return ApiResponse.ok(null);
        return ApiResponse.ok(tenantRepository.findById(p.getTenantId()).orElse(null));
    }

    @PostMapping
    @Transactional
    public ApiResponse<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        requireSuperAdmin();
        String code = str(body.get("code"));
        String name = str(body.get("name"));
        if (code == null || code.isBlank() || name == null || name.isBlank()) {
            throw new BusinessException("code 和 name 必填");
        }
        code = code.trim();
        if (!code.matches("[a-z0-9_]{2,50}")) throw new BusinessException("租户编码仅允许小写字母、数字、下划线，长度 2-50");
        if (tenantRepository.existsByCode(code)) throw new BusinessException("租户编码已存在");

        Tenant t = new Tenant();
        t.setCode(code);
        t.setName(name.trim());
        t.setContact(str(body.get("contact")));
        t.setPlan(body.getOrDefault("plan", "STANDARD").toString());
        t.setStatus("ACTIVE");
        if (body.get("expireAt") != null && !str(body.get("expireAt")).isBlank()) {
            t.setExpireAt(LocalDateTime.parse(str(body.get("expireAt"))));
        }
        tenantRepository.save(t);

        // Optionally provision the tenant's first admin so it is immediately usable.
        String adminUser = str(body.get("adminUsername"));
        String adminPass = str(body.get("adminPassword"));
        if (adminUser != null && !adminUser.isBlank()) {
            if (adminPass == null || adminPass.isBlank()) throw new BusinessException("请为初始管理员设置密码");
            if (userRepository.findByTenantIdAndUsername(t.getId(), adminUser).isPresent()) {
                throw new BusinessException("该租户下管理员用户名已存在");
            }
            User u = new User();
            u.setTenantId(t.getId());
            u.setUsername(adminUser.trim());
            u.setPassword(passwordEncoder.encode(adminPass));
            u.setNickname(str(body.get("adminNickname")) != null ? str(body.get("adminNickname")) : adminUser.trim());
            u.setRole("ADMIN");
            u.setStatus("ACTIVE");
            userRepository.save(u);
        }
        return ApiResponse.ok(toView(t));
    }

    @PutMapping("/{id}")
    public ApiResponse<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        requireSuperAdmin();
        Tenant t = tenantRepository.findById(id).orElseThrow(() -> new BusinessException(404, "租户不存在"));
        if (body.get("name") != null) t.setName(body.get("name").toString());
        if (body.get("contact") != null) t.setContact(body.get("contact").toString());
        if (body.get("plan") != null) t.setPlan(body.get("plan").toString());
        if (body.get("status") != null) {
            String s = body.get("status").toString();
            if (!"ACTIVE".equals(s) && !"DISABLED".equals(s)) throw new BusinessException("状态非法");
            t.setStatus(s);
        }
        if (body.containsKey("expireAt")) {
            String e = str(body.get("expireAt"));
            t.setExpireAt(e == null || e.isBlank() ? null : LocalDateTime.parse(e));
        }
        return ApiResponse.ok(toView(tenantRepository.save(t)));
    }

    private Map<String, Object> toView(Tenant t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", t.getId());
        m.put("name", t.getName());
        m.put("code", t.getCode());
        m.put("status", t.getStatus());
        m.put("plan", t.getPlan());
        m.put("contact", t.getContact());
        m.put("createdAt", t.getCreatedAt());
        m.put("expireAt", t.getExpireAt());
        m.put("userCount", userRepository.countByTenantId(t.getId()));
        m.put("appCount", appRepository.countByTenantId(t.getId()));
        m.put("eventCount", eventRepository.countByTenantId(t.getId()));
        return m;
    }

    private String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }
}
