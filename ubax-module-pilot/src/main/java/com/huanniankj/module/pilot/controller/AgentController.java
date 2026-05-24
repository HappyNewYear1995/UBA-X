package com.huanniankj.module.pilot.controller;

import com.huanniankj.framework.common.pojo.CommonResult;
import com.huanniankj.module.pilot.controller.vo.EventAnalysisReqVO;
import com.huanniankj.module.pilot.dal.model.HeartbeatPayload;
import com.huanniankj.module.pilot.service.AgentsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@Tag(name = "管理后台 - Agent控制层", description = "ubax-pilot 相关接口")
@RestController
@RequestMapping("/pilot/agent")
@Validated
public class AgentController {

    @Resource
    private AgentsService agentsService;

    @GetMapping("/config")
    @Operation(summary = "ubax-pilot 周期性拉取配置", description = "ubax-pilot 周期性拉取配置")
    @PermitAll
    public CommonResult<String> getConfig(@RequestHeader(value = "X-Hostname", defaultValue = "unknown") String hostnameO) {
        // 返回 Vector YAML 配置规则
        return CommonResult.success("""
                sources:
                  app_logs:
                    type: "file"
                    include: ["/var/log/app/*.log"]
                    read_from: "beginning"
                
                sinks:
                  ubax_server:
                    type: "http"
                    inputs: ["app_logs"]
                    uri: "http://your-server/api/logs"
                    encoding:
                      codec: "json"
                """);
    }

    @PostMapping("/heartbeat")
    @Operation(summary = "接收 ubax-pilot 心跳上报", description = "接收 ubax-pilot 心跳上报")
    @PermitAll
    public CommonResult<Map<String, String>> heartbeat(@RequestBody HeartbeatPayload payload) {
        agentsService.updateHeartbeat(payload);
        return CommonResult.success(Map.of("status", "ok"));
    }

    @GetMapping("/push")
    @Operation(summary = "SSE 推送流，ubax-pilot 建立长连接接收配置和命令", description = "SSE 推送流，ubax-pilot 建立长连接接收配置和命令")
    @PermitAll
    public CommonResult<SseEmitter> push(@RequestHeader(value = "X-Hostname", defaultValue = "unknown") String hostname) {
        SseEmitter emitter = new SseEmitter(60_000L); // 60 秒超时

        // 注册客户端
        agentsService.registerClient(hostname, emitter);

        // 客户端断开时清理
        emitter.onCompletion(() -> agentsService.removeClient(hostname));
        emitter.onTimeout(() -> agentsService.removeClient(hostname));
        emitter.onError((e) -> agentsService.removeClient(hostname));

        return CommonResult.success(emitter);
    }

    @PostMapping("/admin/push-command")
    @Operation(summary = "向指定 ubax-pilot 推送命令", description = "向指定 ubax-pilot 推送命令")
    @PermitAll
    public CommonResult<Map<String, String>> pushCommand(@RequestParam String hostname, @RequestParam String action) {
        boolean success = agentsService.pushCommand(hostname, action);
        if (success) {
            return CommonResult.success(Map.of("status", "ok", "message", "命令已推送: " + action));
        }
        return CommonResult.success(Map.of("status", "error", "message", "客户端不在线"));
    }

    @GetMapping("/admin/clients")
    @Operation(summary = "查看所有在线客户端", description = "查看所有在线客户端")
    @PermitAll
    public CommonResult<Map<String, HeartbeatPayload>> getClients(@RequestBody @Validated EventAnalysisReqVO reqVO) {
        return CommonResult.success(agentsService.getAllClients());
    }

}
