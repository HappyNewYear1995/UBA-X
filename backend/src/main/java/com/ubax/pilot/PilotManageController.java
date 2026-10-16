package com.ubax.pilot;

import com.ubax.common.ApiResponse;
import com.ubax.pilot.domain.Agent;
import com.ubax.pilot.domain.AgentCommand;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Management plane for operators (JWT authenticated, tenant scoped).
 */
@RestController
@RequestMapping("/api/pilot/manage")
public class PilotManageController {

    private final PilotService pilotService;

    public PilotManageController(PilotService pilotService) {
        this.pilotService = pilotService;
    }

    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> overview() {
        return ApiResponse.ok(pilotService.overview());
    }

    @GetMapping("/agents")
    public ApiResponse<List<Agent>> agents() {
        return ApiResponse.ok(pilotService.listAgents());
    }

    @GetMapping("/agents/{id}")
    public ApiResponse<Agent> agent(@PathVariable Long id) {
        return ApiResponse.ok(pilotService.getAgent(id));
    }

    @PostMapping("/agents")
    @SuppressWarnings("unchecked")
    public ApiResponse<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        String host = (String) body.get("host");
        String tags = (String) body.get("tags");
        Map<String, Object> config = (Map<String, Object>) body.get("config");
        return ApiResponse.ok(pilotService.createAgent(name, host, tags, config));
    }

    @PutMapping("/agents/{id}/config")
    @SuppressWarnings("unchecked")
    public ApiResponse<Agent> updateConfig(@PathVariable Long id, @RequestBody Map<String, Object> config) {
        return ApiResponse.ok(pilotService.updateConfig(id, config));
    }

    @PostMapping("/agents/{id}/command")
    public ApiResponse<AgentCommand> command(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String type = (String) body.getOrDefault("type", "RELOAD_CONFIG");
        String payload = body.get("payload") == null ? null : body.get("payload").toString();
        return ApiResponse.ok(pilotService.sendCommand(id, type, payload));
    }

    @GetMapping("/agents/{id}/commands")
    public ApiResponse<List<AgentCommand>> commands(@PathVariable Long id) {
        return ApiResponse.ok(pilotService.commands(id));
    }

    @DeleteMapping("/agents/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        pilotService.deleteAgent(id);
        return ApiResponse.ok();
    }
}
