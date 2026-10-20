package com.ubax.collector;

import com.ubax.app.domain.App;
import com.ubax.collector.dto.EventPayload;
import com.ubax.common.ApiResponse;
import com.ubax.governance.service.IdentityService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/collect")
public class CollectorController {

    private final CollectorService collectorService;
    private final IdentityService identityService;

    public CollectorController(CollectorService collectorService, IdentityService identityService) {
        this.collectorService = collectorService;
        this.identityService = identityService;
    }

    @PostMapping("/event")
    public ApiResponse<Map<String, Object>> event(@RequestBody EventPayload payload, HttpServletRequest req) {
        boolean ok = collectorService.ingest(payload, clientIp(req), req.getHeader("User-Agent"));
        Map<String, Object> data = new HashMap<>();
        data.put("accepted", ok);
        return ApiResponse.ok(data);
    }

    @PostMapping("/batch")
    public ApiResponse<Map<String, Object>> batch(@RequestBody BatchRequest body, HttpServletRequest req) {
        String ip = clientIp(req);
        String ua = req.getHeader("User-Agent");
        int accepted = 0, rejected = 0;
        if (body.getEvents() != null) {
            for (EventPayload p : body.getEvents()) {
                if (p.getAppKey() == null) p.setAppKey(body.getAppKey());
                if (collectorService.ingest(p, ip, ua)) accepted++;
                else rejected++;
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("accepted", accepted);
        data.put("rejected", rejected);
        return ApiResponse.ok(data);
    }

    /** sendBeacon-friendly endpoint accepting a list of events with a shared app key. */
    @PostMapping(value = "/beacon", consumes = {"text/plain", "application/json", "*/*"})
    public ApiResponse<Map<String, Object>> beacon(@RequestBody BatchRequest body, HttpServletRequest req) {
        return batch(body, req);
    }

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> m = new HashMap<>();
        m.put("status", "UP");
        return ApiResponse.ok(m);
    }

    /**
     * Server-side identity binding: folds an anonymous distinctId onto a
     * logged-in userId so pre/post-login events map to one profile. Open
     * endpoint (no auth), appKey-scoped.
     */
    @PostMapping("/identify")
    public ApiResponse<Map<String, Object>> identify(@RequestBody IdentifyRequest body) {
        Map<String, Object> data = new HashMap<>();
        App app = collectorService.resolveApp(body.getAppKey());
        if (app == null || !"ACTIVE".equals(app.getStatus())) {
            data.put("bound", false);
            return ApiResponse.ok(data);
        }
        try {
            identityService.identify(app.getTenantId(), app.getId(), body.getDistinctId(), body.getUserId());
            data.put("bound", true);
        } catch (Exception ex) {
            data.put("bound", false);
        }
        return ApiResponse.ok(data);
    }

    private String clientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        String real = req.getHeader("X-Real-IP");
        if (real != null && !real.isBlank()) return real;
        return req.getRemoteAddr();
    }

    public static class BatchRequest {
        private String appKey;
        private List<EventPayload> events;
        public String getAppKey() { return appKey; }
        public void setAppKey(String appKey) { this.appKey = appKey; }
        public List<EventPayload> getEvents() { return events; }
        public void setEvents(List<EventPayload> events) { this.events = events; }
    }

    public static class IdentifyRequest {
        private String appKey;
        private String distinctId;
        private String userId;
        public String getAppKey() { return appKey; }
        public void setAppKey(String appKey) { this.appKey = appKey; }
        public String getDistinctId() { return distinctId; }
        public void setDistinctId(String distinctId) { this.distinctId = distinctId; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
    }
}
