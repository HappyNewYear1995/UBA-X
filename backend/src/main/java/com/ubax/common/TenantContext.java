package com.ubax.common;

import java.util.List;

/**
 * Holds the authenticated principal's tenant/user context for the current request thread.
 * Central to multi-tenant isolation: services scope all queries by {@link #getTenantId()}.
 */
public class TenantContext {

    public static class Principal {
        private final Long userId;
        private final Long tenantId;
        private final String username;
        private final List<String> roles;

        public Principal(Long userId, Long tenantId, String username, List<String> roles) {
            this.userId = userId;
            this.tenantId = tenantId;
            this.username = username;
            this.roles = roles;
        }

        public Long getUserId() { return userId; }
        public Long getTenantId() { return tenantId; }
        public String getUsername() { return username; }
        public List<String> getRoles() { return roles; }

        public boolean hasRole(String role) {
            return roles != null && roles.contains(role);
        }
    }

    private static final ThreadLocal<Principal> HOLDER = new ThreadLocal<>();

    public static void set(Principal principal) { HOLDER.set(principal); }
    public static Principal get() { return HOLDER.get(); }
    public static void clear() { HOLDER.remove(); }

    public static Long getTenantId() {
        Principal p = HOLDER.get();
        return p == null ? null : p.getTenantId();
    }

    public static Long requireTenantId() {
        Long t = getTenantId();
        if (t == null) throw new BusinessException(401, "未认证");
        return t;
    }

    public static Long getUserId() {
        Principal p = HOLDER.get();
        return p == null ? null : p.getUserId();
    }
}
