package com.ubax.tenant.controller;

import com.ubax.common.ApiResponse;
import com.ubax.common.BusinessException;
import com.ubax.common.TenantContext;
import com.ubax.tenant.PermissionCatalog;
import com.ubax.tenant.domain.Role;
import com.ubax.tenant.repo.RoleRepository;
import com.ubax.tenant.repo.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Role management. Roles are global (shared across tenants); the role code is the PK.
 * Mutating operations are restricted to SUPER_ADMIN. Built-in roles cannot be deleted
 * or have their code changed.
 */
@RestController
@RequestMapping("/api/roles")
public class RoleController {

    /** Role codes shipped with the platform; protected from deletion. */
    private static final Set<String> BUILT_IN =
            Set.of("SUPER_ADMIN", "ADMIN", "ANALYST", "VIEWER");

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public RoleController(RoleRepository roleRepository, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    private void requireSuperAdmin() {
        TenantContext.Principal p = TenantContext.get();
        if (p == null) throw new BusinessException(401, "未认证");
        if (!p.hasRole("SUPER_ADMIN")) throw new BusinessException(403, "仅平台管理员可管理角色");
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list() {
        List<Role> roles = roleRepository.findAll();
        // sort: built-in first (in canonical order), then custom by code
        List<String> order = List.of("SUPER_ADMIN", "ADMIN", "ANALYST", "VIEWER");
        Comparator<Role> cmp = Comparator
                .comparingInt((Role r) -> {
                    int i = order.indexOf(r.getCode());
                    return i < 0 ? order.size() : i;
                })
                .thenComparing(Role::getCode);
        roles.sort(cmp);
        List<Map<String, Object>> out = new ArrayList<>();
        for (Role r : roles) {
            out.add(toView(r));
        }
        return ApiResponse.ok(out);
    }

    /** The permission catalog, grouped, for the role editor matrix. */
    @GetMapping("/permissions")
    public ApiResponse<List<Map<String, Object>>> permissions() {
        return ApiResponse.ok(PermissionCatalog.asView());
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        requireSuperAdmin();
        String code = str(body.get("code"));
        if (code == null || code.isBlank()) throw new BusinessException("角色编码必填");
        code = code.trim().toUpperCase(Locale.ROOT);
        if (!code.matches("[A-Z0-9_]{2,20}")) throw new BusinessException("角色编码仅允许大写字母、数字、下划线，长度 2-20");
        if (roleRepository.existsById(code)) throw new BusinessException("角色编码已存在");
        String name = str(body.get("name"));
        if (name == null || name.isBlank()) throw new BusinessException("角色名称必填");

        Role r = new Role();
        r.setCode(code);
        r.setName(name.trim());
        r.setDescription(str(body.get("description")));
        r.setPermissions(normalizePermissions(body.get("permissions")));
        return ApiResponse.ok(toView(roleRepository.save(r)));
    }

    @PutMapping("/{code}")
    public ApiResponse<Map<String, Object>> update(@PathVariable String code, @RequestBody Map<String, Object> body) {
        requireSuperAdmin();
        Role r = roleRepository.findById(code).orElseThrow(() -> new BusinessException(404, "角色不存在"));
        if (body.get("name") != null) {
            String name = str(body.get("name"));
            if (name != null && !name.isBlank()) r.setName(name.trim());
        }
        if (body.containsKey("description")) r.setDescription(str(body.get("description")));
        if (body.containsKey("permissions")) {
            String perms = normalizePermissions(body.get("permissions"));
            // The platform super-admin role must always retain full control.
            if ("SUPER_ADMIN".equals(code)) {
                Set<String> set = new LinkedHashSet<>(split(perms));
                set.add("tenant:manage");
                set.add("system:manage");
                perms = String.join(",", set);
            }
            r.setPermissions(perms);
        }
        return ApiResponse.ok(toView(roleRepository.save(r)));
    }

    @DeleteMapping("/{code}")
    public ApiResponse<Void> delete(@PathVariable String code) {
        requireSuperAdmin();
        if (BUILT_IN.contains(code)) throw new BusinessException("内置角色不可删除");
        if (!roleRepository.existsById(code)) throw new BusinessException(404, "角色不存在");
        long assigned = userRepository.countByRole(code);
        if (assigned > 0) throw new BusinessException("该角色下仍有 " + assigned + " 个用户，请先调整后再删除");
        roleRepository.deleteById(code);
        return ApiResponse.ok();
    }

    private Map<String, Object> toView(Role r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("code", r.getCode());
        m.put("name", r.getName());
        m.put("description", r.getDescription());
        m.put("permissions", split(r.getPermissions()));
        m.put("builtIn", BUILT_IN.contains(r.getCode()));
        m.put("userCount", userRepository.countByRole(r.getCode()));
        return m;
    }

    /** Accepts a List<String> or a CSV String and returns a normalized CSV of valid codes. */
    private String normalizePermissions(Object raw) {
        List<String> codes;
        if (raw instanceof Collection<?> c) {
            codes = c.stream().map(String::valueOf).collect(Collectors.toList());
        } else if (raw != null) {
            codes = split(String.valueOf(raw));
        } else {
            codes = List.of();
        }
        LinkedHashSet<String> out = new LinkedHashSet<>();
        for (String code : codes) {
            String t = code == null ? "" : code.trim();
            if (!t.isEmpty() && PermissionCatalog.isValid(t)) out.add(t);
        }
        return String.join(",", out);
    }

    private List<String> split(String csv) {
        if (csv == null || csv.isBlank()) return new ArrayList<>();
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }
}
