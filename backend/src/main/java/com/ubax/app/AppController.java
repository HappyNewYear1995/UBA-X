package com.ubax.app;

import com.ubax.app.domain.App;
import com.ubax.common.ApiResponse;
import com.ubax.system.AuditService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/apps")
public class AppController {

    private final AppService appService;
    private final AuditService auditService;

    public AppController(AppService appService, AuditService auditService) {
        this.appService = appService;
        this.auditService = auditService;
    }

    @GetMapping
    public ApiResponse<List<App>> list() {
        return ApiResponse.ok(appService.listForCurrentTenant());
    }

    @GetMapping("/{id}")
    public ApiResponse<App> get(@PathVariable Long id) {
        return ApiResponse.ok(appService.get(id));
    }

    @PostMapping
    public ApiResponse<App> create(@RequestBody App app) {
        App saved = appService.create(app);
        auditService.record("CREATE", "应用管理", "新建应用", "应用=" + saved.getName());
        return ApiResponse.ok(saved);
    }

    @PutMapping("/{id}")
    public ApiResponse<App> update(@PathVariable Long id, @RequestBody App app) {
        App saved = appService.update(id, app);
        auditService.record("UPDATE", "应用管理", "编辑应用", "应用=" + saved.getName());
        return ApiResponse.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        String name;
        try { name = appService.get(id).getName(); } catch (Exception e) { name = String.valueOf(id); }
        appService.delete(id);
        auditService.record("DELETE", "应用管理", "删除应用", "应用=" + name);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/rotate-key")
    public ApiResponse<App> rotate(@PathVariable Long id) {
        App saved = appService.rotateKey(id);
        auditService.record("UPDATE", "应用管理", "重置AppKey", "应用=" + saved.getName());
        return ApiResponse.ok(saved);
    }

    /** Returns the embeddable SDK snippet for this app. */
    @GetMapping("/{id}/sdk")
    public ApiResponse<Map<String, Object>> sdk(@PathVariable Long id,
                                                @RequestParam(defaultValue = "") String server) {
        App app = appService.get(id);
        String base = (server == null || server.isBlank()) ? "" : server;
        String snippet = "<script>\n" +
                "  (function(w,d,s){\n" +
                "    w.UBAX_CONFIG={appKey:'" + app.getAppKey() + "',server:'" + base + "',autoTrack:" + app.getAutoTrack() + "};\n" +
                "    var f=d.getElementsByTagName(s)[0],j=d.createElement(s);\n" +
                "    j.async=true;j.src='" + base + "/sdk/ubax.js';f.parentNode.insertBefore(j,f);\n" +
                "  })(window,document,'script');\n" +
                "</script>";
        Map<String, Object> m = new HashMap<>();
        m.put("appKey", app.getAppKey());
        m.put("platform", app.getPlatform());
        m.put("snippet", snippet);
        m.put("sdkUrl", base + "/sdk/ubax.js");
        return ApiResponse.ok(m);
    }
}
