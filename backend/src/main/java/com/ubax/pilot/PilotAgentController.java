package com.ubax.pilot;

import com.ubax.common.ApiResponse;
import com.ubax.system.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Agent-facing plane. Authenticated by the per-agent key in the request body,
 * so these endpoints are open in the security filter chain. The server also
 * observes the probe's remote IP so NAT'd agents can be located even when they
 * cannot self-report a public address.
 */
@RestController
@RequestMapping("/api/pilot/agents")
public class PilotAgentController {

    private final PilotService pilotService;

    public PilotAgentController(PilotService pilotService) {
        this.pilotService = pilotService;
    }

    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@RequestBody Map<String, Object> body,
                                                     HttpServletRequest request) {
        return ApiResponse.ok(pilotService.register(body, AuditService.clientIp(request)));
    }

    @PostMapping("/heartbeat")
    public ApiResponse<Map<String, Object>> heartbeat(@RequestBody Map<String, Object> body,
                                                      HttpServletRequest request) {
        return ApiResponse.ok(pilotService.heartbeat(body, AuditService.clientIp(request)));
    }

    @PostMapping("/pull")
    public ApiResponse<Map<String, Object>> pull(@RequestBody Map<String, Object> body,
                                                 HttpServletRequest request) {
        return ApiResponse.ok(pilotService.heartbeat(body, AuditService.clientIp(request)));
    }

    @PostMapping("/result")
    public ApiResponse<Void> result(@RequestBody Map<String, Object> body) {
        pilotService.reportResult(body);
        return ApiResponse.ok();
    }
}
