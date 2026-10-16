package com.ubax.ext;

import com.ubax.common.ApiResponse;
import com.ubax.common.TenantContext;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * "数据应用" extension apps. All endpoints are tenant-scoped and accept an optional
 * appId plus a from/to date range (yyyy-MM-dd).
 */
@RestController
@RequestMapping("/api/ext")
public class ExtController {

    private final ExtService extService;
    private final ExtPathService pathService;
    private final ExtUserService userService;
    private final ExtInsightService insightService;
    private final ExtGrowthService growthService;

    public ExtController(ExtService extService, ExtPathService pathService,
                         ExtUserService userService, ExtInsightService insightService,
                         ExtGrowthService growthService) {
        this.extService = extService;
        this.pathService = pathService;
        this.userService = userService;
        this.insightService = insightService;
        this.growthService = growthService;
    }

    private Long tid() { return TenantContext.requireTenantId(); }

    /* ---- original three ---- */

    @GetMapping("/security")
    public ApiResponse<Map<String, Object>> security(@RequestParam(required = false) Long appId,
                                                     @RequestParam(required = false) String from,
                                                     @RequestParam(required = false) String to) {
        return ApiResponse.ok(extService.security(tid(), appId, from, to));
    }

    @GetMapping("/geo")
    public ApiResponse<Map<String, Object>> geo(@RequestParam(required = false) Long appId,
                                                @RequestParam(required = false) String from,
                                                @RequestParam(required = false) String to) {
        return ApiResponse.ok(extService.geo(tid(), appId, from, to));
    }

    @GetMapping("/fingerprint")
    public ApiResponse<Map<String, Object>> fingerprint(@RequestParam(required = false) Long appId,
                                                        @RequestParam(required = false) String from,
                                                        @RequestParam(required = false) String to) {
        return ApiResponse.ok(extService.fingerprint(tid(), appId, from, to));
    }

    /* ---- conversion & funnel ---- */

    @GetMapping("/conversion-path")
    public ApiResponse<Map<String, Object>> conversionPath(@RequestParam(required = false) Long appId,
                                                           @RequestParam(required = false) String from,
                                                           @RequestParam(required = false) String to,
                                                           @RequestParam(required = false) String goal) {
        return ApiResponse.ok(pathService.conversionPath(tid(), appId, from, to, goal));
    }

    @GetMapping("/funnel-compare")
    public ApiResponse<Map<String, Object>> funnelCompare(@RequestParam(required = false) Long appId,
                                                          @RequestParam(required = false) String from,
                                                          @RequestParam(required = false) String to,
                                                          @RequestParam(required = false) String dim) {
        return ApiResponse.ok(pathService.funnelCompare(tid(), appId, from, to, dim));
    }

    /* ---- user: retention / churn / rfm / journey ---- */

    @GetMapping("/retention-segments")
    public ApiResponse<Map<String, Object>> retentionSegments(@RequestParam(required = false) Long appId,
                                                              @RequestParam(required = false) String from,
                                                              @RequestParam(required = false) String to,
                                                              @RequestParam(required = false) String dim) {
        return ApiResponse.ok(userService.retentionSegments(tid(), appId, from, to, dim));
    }

    @GetMapping("/churn-health")
    public ApiResponse<Map<String, Object>> churnHealth(@RequestParam(required = false) Long appId,
                                                        @RequestParam(required = false) String from,
                                                        @RequestParam(required = false) String to) {
        return ApiResponse.ok(userService.churnHealth(tid(), appId, from, to));
    }

    @GetMapping("/rfm")
    public ApiResponse<Map<String, Object>> rfm(@RequestParam(required = false) Long appId,
                                                @RequestParam(required = false) String from,
                                                @RequestParam(required = false) String to) {
        return ApiResponse.ok(userService.rfm(tid(), appId, from, to));
    }

    @GetMapping("/journey-users")
    public ApiResponse<Map<String, Object>> journeyUsers(@RequestParam(required = false) Long appId) {
        return ApiResponse.ok(userService.journeyUsers(tid(), appId));
    }

    @GetMapping("/journey")
    public ApiResponse<Map<String, Object>> journey(@RequestParam String distinctId) {
        return ApiResponse.ok(userService.journey(tid(), distinctId));
    }

    /* ---- insight: anomaly / page experience / event explorer ---- */

    @GetMapping("/anomaly")
    public ApiResponse<Map<String, Object>> anomaly(@RequestParam(required = false) Long appId,
                                                    @RequestParam(required = false) String from,
                                                    @RequestParam(required = false) String to,
                                                    @RequestParam(required = false) String metric) {
        return ApiResponse.ok(insightService.anomaly(tid(), appId, from, to, metric));
    }

    @GetMapping("/page-experience")
    public ApiResponse<Map<String, Object>> pageExperience(@RequestParam(required = false) Long appId,
                                                           @RequestParam(required = false) String from,
                                                           @RequestParam(required = false) String to) {
        return ApiResponse.ok(insightService.pageExperience(tid(), appId, from, to));
    }

    @GetMapping("/event-meta")
    public ApiResponse<Map<String, Object>> eventMeta(@RequestParam(required = false) Long appId,
                                                      @RequestParam(required = false) String from,
                                                      @RequestParam(required = false) String to) {
        return ApiResponse.ok(insightService.eventMeta(tid(), appId, from, to));
    }

    @GetMapping("/event-explore")
    public ApiResponse<Map<String, Object>> eventExplore(@RequestParam(required = false) Long appId,
                                                         @RequestParam(required = false) String from,
                                                         @RequestParam(required = false) String to,
                                                         @RequestParam(required = false) String event,
                                                         @RequestParam(required = false) String dim,
                                                         @RequestParam(required = false) String metric) {
        return ApiResponse.ok(insightService.eventExplore(tid(), appId, from, to, event, dim, metric));
    }

    /* ---- growth: attribution / A-B experiment ---- */

    @GetMapping("/attribution")
    public ApiResponse<Map<String, Object>> attribution(@RequestParam(required = false) Long appId,
                                                        @RequestParam(required = false) String from,
                                                        @RequestParam(required = false) String to) {
        return ApiResponse.ok(growthService.attribution(tid(), appId, from, to));
    }

    @GetMapping("/ab-test")
    public ApiResponse<Map<String, Object>> abTest(@RequestParam(required = false) Long appId,
                                                   @RequestParam(required = false) String from,
                                                   @RequestParam(required = false) String to,
                                                   @RequestParam(required = false) String metric) {
        return ApiResponse.ok(growthService.abTest(tid(), appId, from, to, metric));
    }
}
