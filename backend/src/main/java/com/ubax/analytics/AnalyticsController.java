package com.ubax.analytics;

import com.ubax.common.ApiResponse;
import com.ubax.common.TenantContext;
import com.ubax.stream.StreamEngine;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final StreamEngine streamEngine;

    public AnalyticsController(AnalyticsService analyticsService, StreamEngine streamEngine) {
        this.analyticsService = analyticsService;
        this.streamEngine = streamEngine;
    }

    private Long tid() { return TenantContext.requireTenantId(); }

    /** Clamp a caller-supplied integer into a safe [lo, hi] range. */
    private static int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(v, hi)); }

    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> overview(@RequestParam(required = false) Long appId,
                                                     @RequestParam(required = false) String from,
                                                     @RequestParam(required = false) String to) {
        return ApiResponse.ok(analyticsService.overview(tid(), appId, from, to));
    }

    @GetMapping("/trend")
    public ApiResponse<List<Map<String, Object>>> trend(@RequestParam(required = false) Long appId,
                                                        @RequestParam(required = false) String from,
                                                        @RequestParam(required = false) String to) {
        return ApiResponse.ok(analyticsService.trend(tid(), appId, from, to));
    }

    @GetMapping("/realtime")
    public ApiResponse<Map<String, Object>> realtime(@RequestParam(required = false) Long appId,
                                                     @RequestParam(defaultValue = "30") int minutes) {
        return ApiResponse.ok(analyticsService.realtime(tid(), appId, clamp(minutes, 1, 1440)));
    }

    @GetMapping("/live")
    public ApiResponse<List<Map<String, Object>>> live(@RequestParam(defaultValue = "30") int limit) {
        return ApiResponse.ok(analyticsService.liveFeed(tid(), clamp(limit, 1, 100)));
    }

    @GetMapping("/events")
    public ApiResponse<List<Map<String, Object>>> events(@RequestParam(required = false) Long appId,
                                                         @RequestParam(required = false) String from,
                                                         @RequestParam(required = false) String to,
                                                         @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(analyticsService.topEvents(tid(), appId, from, to, clamp(limit, 1, 100)));
    }

    @GetMapping("/pages")
    public ApiResponse<List<Map<String, Object>>> pages(@RequestParam(required = false) Long appId,
                                                        @RequestParam(required = false) String from,
                                                        @RequestParam(required = false) String to,
                                                        @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(analyticsService.topPages(tid(), appId, from, to, clamp(limit, 1, 100)));
    }

    @GetMapping("/distributions")
    public ApiResponse<Map<String, Object>> distributions(@RequestParam(required = false) Long appId,
                                                          @RequestParam(required = false) String from,
                                                          @RequestParam(required = false) String to) {
        return ApiResponse.ok(analyticsService.distributions(tid(), appId, from, to));
    }

    @PostMapping("/funnel")
    public ApiResponse<Map<String, Object>> funnel(@RequestBody FunnelRequest req) {
        return ApiResponse.ok(analyticsService.funnel(tid(), req.getAppId(), req.getSteps(), req.getFrom(), req.getTo()));
    }

    @GetMapping("/retention")
    public ApiResponse<Map<String, Object>> retention(@RequestParam(required = false) Long appId,
                                                      @RequestParam(required = false) String from,
                                                      @RequestParam(defaultValue = "7") int days) {
        return ApiResponse.ok(analyticsService.retention(tid(), appId, from, clamp(days, 1, 90)));
    }

    @GetMapping("/heatmap")
    public ApiResponse<Map<String, Object>> heatmap(@RequestParam(required = false) Long appId,
                                                    @RequestParam(required = false) String path,
                                                    @RequestParam(required = false) String from,
                                                    @RequestParam(required = false) String to) {
        return ApiResponse.ok(analyticsService.heatmap(tid(), appId, path, from, to));
    }

    @GetMapping("/sessions")
    public ApiResponse<List<Map<String, Object>>> sessions(@RequestParam(required = false) Long appId,
                                                           @RequestParam(required = false) String from,
                                                           @RequestParam(required = false) String to,
                                                           @RequestParam(defaultValue = "30") int limit) {
        return ApiResponse.ok(analyticsService.sessions(tid(), appId, from, to, clamp(limit, 1, 100)));
    }

    @GetMapping("/sessions/{sessionId}")
    public ApiResponse<List<Map<String, Object>>> sessionDetail(@PathVariable String sessionId) {
        return ApiResponse.ok(analyticsService.sessionDetail(tid(), sessionId));
    }

    @GetMapping("/stream-metrics")
    public ApiResponse<Map<String, Object>> streamMetrics() {
        return ApiResponse.ok(streamEngine.metrics());
    }

    public static class FunnelRequest {
        private Long appId;
        private List<String> steps;
        private String from;
        private String to;
        public Long getAppId() { return appId; }
        public void setAppId(Long appId) { this.appId = appId; }
        public List<String> getSteps() { return steps; }
        public void setSteps(List<String> steps) { this.steps = steps; }
        public String getFrom() { return from; }
        public void setFrom(String from) { this.from = from; }
        public String getTo() { return to; }
        public void setTo(String to) { this.to = to; }
    }
}
