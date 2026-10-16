package com.ubax.tenant.service;

import com.ubax.common.BusinessException;
import com.ubax.common.JwtService;
import com.ubax.system.AuditService;
import com.ubax.tenant.domain.Role;
import com.ubax.tenant.domain.Tenant;
import com.ubax.tenant.domain.User;
import com.ubax.tenant.dto.LoginRequest;
import com.ubax.tenant.dto.LoginResponse;
import com.ubax.tenant.repo.RoleRepository;
import com.ubax.tenant.repo.TenantRepository;
import com.ubax.tenant.repo.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditService auditService;

    public AuthService(UserRepository userRepository, TenantRepository tenantRepository,
                       RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
                       AuditService auditService) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditService = auditService;
    }

    public LoginResponse login(LoginRequest req) {
        Optional<User> found;
        Tenant tenant = null;
        if (req.getTenantCode() == null || req.getTenantCode().isBlank()) {
            // platform-level login (super admin)
            found = userRepository.findByUsernameAndTenantIdIsNull(req.getUsername());
            if (found.isEmpty()) {
                // allow default tenant login without code
                found = userRepository.findByTenantIdAndUsername(1L, req.getUsername());
                if (found.isPresent()) tenant = tenantRepository.findById(1L).orElse(null);
            }
        } else {
            tenant = tenantRepository.findByCode(req.getTenantCode())
                    .orElseThrow(() -> {
                        auditService.recordAuth("LOGIN", null, null, req.getUsername(), false, "租户不存在:" + req.getTenantCode());
                        return new BusinessException(401, "租户不存在");
                    });
            if (!"ACTIVE".equals(tenant.getStatus())) throw new BusinessException(403, "租户已停用");
            final Long tid = tenant.getId();
            found = userRepository.findByTenantIdAndUsername(tid, req.getUsername());
        }

        User user = found.orElse(null);
        if (user == null) {
            auditService.recordAuth("LOGIN", tenant != null ? tenant.getId() : null, null, req.getUsername(), false, "用户名或密码错误");
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (!"ACTIVE".equals(user.getStatus())) {
            auditService.recordAuth("LOGIN", user.getTenantId(), user.getId(), user.getUsername(), false, "账号已停用");
            throw new BusinessException(403, "账号已停用");
        }
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            auditService.recordAuth("LOGIN", user.getTenantId(), user.getId(), user.getUsername(), false, "用户名或密码错误");
            throw new BusinessException(401, "用户名或密码错误");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        auditService.recordAuth("LOGIN", user.getTenantId(), user.getId(), user.getUsername(), true, "登录成功");

        String tenantName = tenant != null ? tenant.getName() : (user.getTenantId() == null ? "平台" : null);
        if (tenantName == null && user.getTenantId() != null) {
            tenantName = tenantRepository.findById(user.getTenantId()).map(Tenant::getName).orElse(null);
        }

        String token = jwtService.generate(user.getId(), user.getTenantId(), user.getUsername(),
                List.of(user.getRole()));
        List<String> permissions = permissionsFor(user.getRole());

        return new LoginResponse(token, user.getId(), user.getUsername(), user.getNickname(),
                user.getRole(), user.getTenantId(), tenantName, permissions);
    }

    public List<String> permissionsFor(String roleCode) {
        return roleRepository.findById(roleCode)
                .map(Role::getPermissions)
                .filter(p -> p != null && !p.isBlank())
                .map(p -> Arrays.asList(p.split(",")))
                .orElse(List.of());
    }
}
