package com.ubax.governance.controller;

import com.ubax.common.ApiResponse;
import com.ubax.common.BusinessException;
import com.ubax.common.TenantContext;
import com.ubax.governance.domain.EventDefinition;
import com.ubax.governance.domain.IdentityRule;
import com.ubax.governance.domain.ParsingRule;
import com.ubax.governance.service.EventDefinitionService;
import com.ubax.governance.service.IdentityService;
import com.ubax.governance.service.ParsingRuleService;
import com.ubax.system.AuditService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Collection-governance configuration endpoints (event dictionary, parsing
 * rules, identity rules / alias). Management operations require SUPER_ADMIN or
 * ADMIN. Tenant admins act on their own tenant; the platform super admin
 * (tenantId = null) must pass an explicit tenantId.
 */
@RestController
@RequestMapping("/api/governance")
public class GovernanceController {

    private final EventDefinitionService eventDefinitionService;
    private final ParsingRuleService parsingRuleService;
    private final IdentityService identityService;
    private final AuditService auditService;

    public GovernanceController(EventDefinitionService eventDefinitionService,
                                ParsingRuleService parsingRuleService,
                                IdentityService identityService,
                                AuditService auditService) {
        this.eventDefinitionService = eventDefinitionService;
        this.parsingRuleService = parsingRuleService;
        this.identityService = identityService;
        this.auditService = auditService;
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

    /**
     * Resolve the tenant a request operates on. A tenant admin is pinned to
     * their own tenant (explicit param ignored); the super admin must supply one.
     */
    private Long tenant(Long explicit) {
        Long own = principal().getTenantId();
        if (own != null) return own;
        if (explicit == null) throw new BusinessException(400, "请选择租户");
        return explicit;
    }

    private static int page(int p) { return Math.max(0, p); }

    private static int size(int s) { return Math.max(1, Math.min(s, 200)); }

    // ---------- event dictionary ----------

    @GetMapping("/events")
    public ApiResponse<Map<String, Object>> events(@RequestParam(required = false) Long tenantId,
                                                   @RequestParam(required = false) Long appId,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        requireManage();
        return ApiResponse.ok(eventDefinitionService.page(tenant(tenantId), appId, page(page), size(size)));
    }

    @PostMapping("/events")
    public ApiResponse<EventDefinition> createEvent(@RequestBody EventDefinition d,
                                                    @RequestParam(required = false) Long tenantId) {
        requireManage();
        Long tid = tenant(tenantId != null ? tenantId : d.getTenantId());
        EventDefinition saved = eventDefinitionService.save(d, tid);
        auditService.record("CREATE", "采集治理", "新增事件定义", saved.getEventKey());
        return ApiResponse.ok(saved);
    }

    @PutMapping("/events/{id}")
    public ApiResponse<EventDefinition> updateEvent(@PathVariable Long id, @RequestBody EventDefinition d,
                                                    @RequestParam(required = false) Long tenantId) {
        requireManage();
        Long tid = tenant(tenantId != null ? tenantId : d.getTenantId());
        d.setId(id);
        EventDefinition saved = eventDefinitionService.save(d, tid);
        auditService.record("UPDATE", "采集治理", "更新事件定义", saved.getEventKey());
        return ApiResponse.ok(saved);
    }

    @PatchMapping("/events/{id}/toggle")
    public ApiResponse<EventDefinition> toggleEvent(@PathVariable Long id, @RequestBody Map<String, Object> body,
                                                    @RequestParam(required = false) Long tenantId) {
        requireManage();
        boolean enabled = Boolean.parseBoolean(String.valueOf(body.getOrDefault("enabled", true)));
        EventDefinition saved = eventDefinitionService.toggle(id, enabled, tenant(tenantId));
        auditService.record("UPDATE", "采集治理", (enabled ? "启用" : "停用") + "事件定义", "id=" + id);
        return ApiResponse.ok(saved);
    }

    @DeleteMapping("/events/{id}")
    public ApiResponse<Void> deleteEvent(@PathVariable Long id, @RequestParam(required = false) Long tenantId) {
        requireManage();
        eventDefinitionService.delete(id, tenant(tenantId));
        auditService.record("DELETE", "采集治理", "删除事件定义", "id=" + id);
        return ApiResponse.ok();
    }

    // ---------- parsing rules ----------

    @GetMapping("/rules")
    public ApiResponse<Map<String, Object>> rules(@RequestParam(required = false) Long tenantId,
                                                  @RequestParam(required = false) Long appId,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        requireManage();
        return ApiResponse.ok(parsingRuleService.page(tenant(tenantId), appId, page(page), size(size)));
    }

    @PostMapping("/rules")
    public ApiResponse<ParsingRule> createRule(@RequestBody ParsingRule r,
                                               @RequestParam(required = false) Long tenantId) {
        requireManage();
        Long tid = tenant(tenantId != null ? tenantId : r.getTenantId());
        ParsingRule saved = parsingRuleService.save(r, tid);
        auditService.record("CREATE", "采集治理", "新增解析规则", saved.getName());
        return ApiResponse.ok(saved);
    }

    @PutMapping("/rules/{id}")
    public ApiResponse<ParsingRule> updateRule(@PathVariable Long id, @RequestBody ParsingRule r,
                                               @RequestParam(required = false) Long tenantId) {
        requireManage();
        Long tid = tenant(tenantId != null ? tenantId : r.getTenantId());
        r.setId(id);
        ParsingRule saved = parsingRuleService.save(r, tid);
        auditService.record("UPDATE", "采集治理", "更新解析规则", saved.getName());
        return ApiResponse.ok(saved);
    }

    @PatchMapping("/rules/{id}/toggle")
    public ApiResponse<ParsingRule> toggleRule(@PathVariable Long id, @RequestBody Map<String, Object> body,
                                               @RequestParam(required = false) Long tenantId) {
        requireManage();
        boolean enabled = Boolean.parseBoolean(String.valueOf(body.getOrDefault("enabled", true)));
        ParsingRule saved = parsingRuleService.toggle(id, enabled, tenant(tenantId));
        auditService.record("UPDATE", "采集治理", (enabled ? "启用" : "停用") + "解析规则", "id=" + id);
        return ApiResponse.ok(saved);
    }

    @DeleteMapping("/rules/{id}")
    public ApiResponse<Void> deleteRule(@PathVariable Long id, @RequestParam(required = false) Long tenantId) {
        requireManage();
        parsingRuleService.delete(id, tenant(tenantId));
        auditService.record("DELETE", "采集治理", "删除解析规则", "id=" + id);
        return ApiResponse.ok();
    }

    // ---------- identity ----------

    @GetMapping("/identity")
    public ApiResponse<List<IdentityRule>> identity(@RequestParam(required = false) Long tenantId) {
        requireManage();
        return ApiResponse.ok(identityService.rules(tenant(tenantId)));
    }

    @PostMapping("/identity")
    public ApiResponse<IdentityRule> saveIdentity(@RequestBody IdentityRule r,
                                                  @RequestParam(required = false) Long tenantId) {
        requireManage();
        Long tid = tenant(tenantId != null ? tenantId : r.getTenantId());
        IdentityRule saved = identityService.saveRule(r, tid);
        auditService.record("UPDATE", "采集治理", "保存用户标识策略", saved.getStrategy());
        return ApiResponse.ok(saved);
    }

    @DeleteMapping("/identity/{id}")
    public ApiResponse<Void> deleteIdentity(@PathVariable Long id, @RequestParam(required = false) Long tenantId) {
        requireManage();
        identityService.deleteRule(id, tenant(tenantId));
        auditService.record("DELETE", "采集治理", "删除用户标识策略", "id=" + id);
        return ApiResponse.ok();
    }

    @GetMapping("/alias")
    public ApiResponse<Map<String, Object>> alias(@RequestParam(required = false) Long tenantId,
                                                  @RequestParam(required = false) Long appId,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        requireManage();
        return ApiResponse.ok(identityService.aliasPage(tenant(tenantId), appId, page(page), size(size)));
    }
}
