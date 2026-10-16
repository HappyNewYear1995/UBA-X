package com.ubax.system;

import com.ubax.common.TenantContext;
import com.ubax.system.domain.AuditLog;
import com.ubax.system.repo.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Writes and queries the audit trail. Login events are recorded from AuthService
 * (before the principal exists, so identity is passed explicitly); business
 * operations pull the caller from {@link TenantContext}.
 */
@Service
public class AuditService {

    private final AuditLogRepository repo;

    public AuditService(AuditLogRepository repo) {
        this.repo = repo;
    }

    // ---------- recording ----------

    /** Record a business operation for the current principal. */
    public void record(String type, String module, String action, String detail) {
        TenantContext.Principal p = TenantContext.get();
        AuditLog a = base();
        if (p != null) {
            a.setTenantId(p.getTenantId());
            a.setUserId(p.getUserId());
            a.setUsername(p.getUsername());
        }
        a.setType(type);
        a.setModule(module);
        a.setAction(action);
        a.setDetail(truncate(detail, 600));
        a.setSuccess(true);
        repo.save(a);
    }

    /** Record a login/logout where the principal may not yet be in context. */
    public void recordAuth(String type, Long tenantId, Long userId, String username, boolean success, String detail) {
        AuditLog a = base();
        a.setTenantId(tenantId);
        a.setUserId(userId);
        a.setUsername(username);
        a.setType(type);
        a.setModule("认证");
        a.setAction(success ? ("LOGIN".equals(type) ? "登录成功" : "退出登录") : "登录失败");
        a.setDetail(truncate(detail, 600));
        a.setSuccess(success);
        repo.save(a);
    }

    private AuditLog base() {
        AuditLog a = new AuditLog();
        HttpServletRequest req = currentRequest();
        if (req != null) {
            String ip = clientIp(req);
            a.setIp(ip);
            a.setLocation(locate(ip));
            a.setUserAgent(truncate(req.getHeader("User-Agent"), 300));
        }
        a.setCreatedAt(LocalDateTime.now());
        return a;
    }

    // ---------- querying ----------

    public Map<String, Object> operations(Long tid, String kw, String type, String module,
                                          LocalDateTime from, LocalDateTime to, int page, int size) {
        Page<AuditLog> p = repo.searchOperations(tid, blank(kw), blank(type), blank(module), from, to,
                PageRequest.of(page, size));
        return pageView(p, page, size);
    }

    public Map<String, Object> logins(Long tid, String kw, Boolean success,
                                      LocalDateTime from, LocalDateTime to, int page, int size) {
        Page<AuditLog> p = repo.searchLogins(tid, blank(kw), success, from, to, PageRequest.of(page, size));
        return pageView(p, page, size);
    }

    public Map<String, Object> myLogins(Long userId, int limit) {
        var list = repo.findLoginsByUser(userId, PageRequest.of(0, limit));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("rows", list);
        m.put("total", list.size());
        return m;
    }

    public List<String> distinctModulesSafe(Long tid) {
        return repo.distinctModules(tid);
    }

    private Map<String, Object> pageView(Page<AuditLog> p, int page, int size) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("rows", p.getContent());
        m.put("total", p.getTotalElements());
        m.put("page", page);
        m.put("size", size);
        return m;
    }

    // ---------- helpers ----------

    public static HttpServletRequest currentRequest() {
        var attrs = RequestContextHolder.getRequestAttributes();
        return attrs instanceof ServletRequestAttributes sra ? sra.getRequest() : null;
    }

    public static String clientIp(HttpServletRequest req) {
        String xf = req.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) return xf.split(",")[0].trim();
        String real = req.getHeader("X-Real-IP");
        if (real != null && !real.isBlank()) return real;
        return req.getRemoteAddr();
    }

    public static String locate(String ip) {
        if (ip == null || ip.isBlank()) return "未知";
        if (ip.startsWith("127.") || ip.startsWith("10.") || ip.startsWith("192.168.")
                || ip.startsWith("172.") || ip.contains("0:0:0:0") || "localhost".equalsIgnoreCase(ip)) {
            return "内网/本机";
        }
        return "外部";
    }

    private static String blank(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}
