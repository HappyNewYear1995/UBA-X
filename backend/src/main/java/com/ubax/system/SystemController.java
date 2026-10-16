package com.ubax.system;

import com.ubax.common.ApiResponse;
import com.ubax.common.BusinessException;
import com.ubax.common.TenantContext;
import com.ubax.system.domain.Dict;
import com.ubax.system.domain.Notice;
import com.ubax.system.domain.ScheduledTask;
import com.ubax.system.domain.TaskLog;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * System-management endpoints (audit logs, config, dict, notices, monitor, tasks).
 * Write/management operations require SUPER_ADMIN or ADMIN. All endpoints tolerate a
 * null tenant (platform super admin) instead of throwing 401.
 */
@RestController
@RequestMapping("/api/system")
public class SystemController {

    private final AuditService auditService;
    private final SystemService systemService;
    private final MonitorService monitorService;
    private final TaskService taskService;

    public SystemController(AuditService auditService, SystemService systemService,
                            MonitorService monitorService, TaskService taskService) {
        this.auditService = auditService;
        this.systemService = systemService;
        this.monitorService = monitorService;
        this.taskService = taskService;
    }

    private TenantContext.Principal principal() {
        TenantContext.Principal p = TenantContext.get();
        if (p == null) throw new BusinessException(401, "未认证");
        return p;
    }

    private void requireManage() {
        TenantContext.Principal p = principal();
        if (!(p.hasRole("SUPER_ADMIN") || p.hasRole("ADMIN"))) throw new BusinessException(403, "无权限");
    }

    /** Tenant scope for log queries: null (super admin) means all tenants. */
    private Long scopeTenant() {
        return principal().getTenantId();
    }

    private static LocalDateTime startOf(String date) {
        return (date == null || date.isBlank()) ? null : LocalDate.parse(date).atStartOfDay();
    }

    private static LocalDateTime endOf(String date) {
        return (date == null || date.isBlank()) ? null : LocalDate.parse(date).atTime(LocalTime.MAX);
    }

    // ---------- audit / login logs ----------

    @GetMapping("/audit")
    public ApiResponse<Map<String, Object>> audit(@RequestParam(required = false) String kw,
                                                  @RequestParam(required = false) String type,
                                                  @RequestParam(required = false) String module,
                                                  @RequestParam(required = false) String from,
                                                  @RequestParam(required = false) String to,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        requireManage();
        return ApiResponse.ok(auditService.operations(scopeTenant(), kw, type, module,
                startOf(from), endOf(to), Math.max(0, page), Math.max(1, Math.min(size, 200))));
    }

    @GetMapping("/audit/modules")
    public ApiResponse<List<String>> modules() {
        requireManage();
        return ApiResponse.ok(auditService.distinctModulesSafe(scopeTenant()));
    }

    @GetMapping("/login-log")
    public ApiResponse<Map<String, Object>> loginLog(@RequestParam(required = false) String kw,
                                                     @RequestParam(required = false) Boolean success,
                                                     @RequestParam(required = false) String from,
                                                     @RequestParam(required = false) String to,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        requireManage();
        return ApiResponse.ok(auditService.logins(scopeTenant(), kw, success,
                startOf(from), endOf(to), Math.max(0, page), Math.max(1, Math.min(size, 200))));
    }

    // ---------- config ----------

    @GetMapping("/config")
    public ApiResponse<List<Map<String, Object>>> config() {
        requireManage();
        return ApiResponse.ok(systemService.groupedConfig());
    }

    @PutMapping("/config")
    public ApiResponse<List<?>> saveConfig(@RequestBody List<Map<String, String>> edits) {
        requireManage();
        List<?> saved = systemService.saveAll(edits);
        auditService.record("UPDATE", "系统配置", "修改参数", "共 " + saved.size() + " 项");
        return ApiResponse.ok(saved);
    }

    // ---------- dict ----------

    @GetMapping("/dict/types")
    public ApiResponse<List<String>> dictTypes() {
        requireManage();
        return ApiResponse.ok(systemService.dictTypes());
    }

    @GetMapping("/dict")
    public ApiResponse<List<Dict>> dict(@RequestParam String type) {
        requireManage();
        return ApiResponse.ok(systemService.dictByType(type));
    }

    @PostMapping("/dict")
    public ApiResponse<Dict> saveDict(@RequestBody Dict d) {
        requireManage();
        boolean isNew = d.getId() == null;
        Dict saved = systemService.saveDict(d);
        auditService.record(isNew ? "CREATE" : "UPDATE", "数据字典",
                "保存字典项", saved.getDictType() + ":" + saved.getDictLabel());
        return ApiResponse.ok(saved);
    }

    @DeleteMapping("/dict/{id}")
    public ApiResponse<Void> deleteDict(@PathVariable Long id) {
        requireManage();
        systemService.deleteDict(id);
        auditService.record("DELETE", "数据字典", "删除字典项", "id=" + id);
        return ApiResponse.ok();
    }

    // ---------- notice ----------

    @GetMapping("/notice")
    public ApiResponse<List<Notice>> notices() {
        requireManage();
        return ApiResponse.ok(systemService.listNotice());
    }

    /** Published notices for the header bell — any authenticated user. */
    @GetMapping("/notice/published")
    public ApiResponse<List<Notice>> publishedNotices() {
        principal();
        return ApiResponse.ok(systemService.publishedNotice());
    }

    @PostMapping("/notice")
    public ApiResponse<Notice> saveNotice(@RequestBody Notice n) {
        requireManage();
        boolean isNew = n.getId() == null;
        Notice saved = systemService.saveNotice(n, principal().getUsername());
        auditService.record(isNew ? "CREATE" : "UPDATE", "通知公告",
                "PUBLISHED".equals(saved.getStatus()) ? "发布公告" : "保存公告", saved.getTitle());
        return ApiResponse.ok(saved);
    }

    @DeleteMapping("/notice/{id}")
    public ApiResponse<Void> deleteNotice(@PathVariable Long id) {
        requireManage();
        systemService.deleteNotice(id);
        auditService.record("DELETE", "通知公告", "删除公告", "id=" + id);
        return ApiResponse.ok();
    }

    // ---------- monitor ----------

    @GetMapping("/monitor")
    public ApiResponse<Map<String, Object>> monitor() {
        requireManage();
        return ApiResponse.ok(monitorService.snapshot(scopeTenant()));
    }

    // ---------- scheduled tasks ----------

    @GetMapping("/task")
    public ApiResponse<List<ScheduledTask>> tasks() {
        requireManage();
        return ApiResponse.ok(taskService.list());
    }

    @GetMapping("/task/logs")
    public ApiResponse<List<TaskLog>> taskLogs(@RequestParam(required = false) String code,
                                               @RequestParam(defaultValue = "50") int limit) {
        requireManage();
        return ApiResponse.ok(taskService.logs(code, Math.min(limit, 200)));
    }

    @PutMapping("/task/{id}/toggle")
    public ApiResponse<ScheduledTask> toggleTask(@PathVariable Long id, @RequestParam boolean enabled) {
        requireManage();
        ScheduledTask t = taskService.toggle(id, enabled);
        auditService.record("UPDATE", "定时任务", enabled ? "启用任务" : "停用任务", t.getName());
        return ApiResponse.ok(t);
    }

    @PostMapping("/task/{id}/run")
    public ApiResponse<TaskLog> runTask(@PathVariable Long id) {
        requireManage();
        TaskLog tl = taskService.runNow(id);
        auditService.record("OTHER", "定时任务", "手动执行任务", tl.getTaskName() + " → " + tl.getStatus());
        return ApiResponse.ok(tl);
    }
}
