package com.ubax.collector;

import com.ubax.collector.dto.EventPayload;
import com.ubax.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/collect")
public class CollectorController {

    private final CollectorService collectorService;

    public CollectorController(CollectorService collectorService) {
        this.collectorService = collectorService;
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
}
