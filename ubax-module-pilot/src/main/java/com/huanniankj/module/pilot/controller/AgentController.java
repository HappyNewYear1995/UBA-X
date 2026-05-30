package com.huanniankj.module.pilot.controller;

import com.huanniankj.framework.common.pojo.CommonResult;
import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.pilot.controller.vo.*;
import com.huanniankj.module.pilot.service.AgentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static com.huanniankj.framework.common.pojo.CommonResult.success;

/**
 * Agent 控制层
 *
 * @author zhaoff
 */
@Slf4j
@Tag(name = "Agent 管理", description = "Agent 探针管理接口")
@RestController
@RequestMapping("/pilot/agent")
@Validated
public class AgentController {

    @Resource
    private AgentService agentService;

    @GetMapping("/page")
    @Operation(summary = "获得 Agent 分页列表", description = "用于管理后台查看 Agent 列表")
    @PermitAll
    public CommonResult<PageResult<AgentRespVO>> getAgentPage(@Validated AgentPageReqVO pageReqVO) {
        return success(agentService.getAgentPage(pageReqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获得 Agent 详情", description = "用于管理后台查看 Agent 详情")
    @Parameter(name = "id", description = "Agent ID", required = true, example = "1")
    @PermitAll
    public CommonResult<AgentRespVO> getAgent(@RequestParam("id") Long id) {
        return success(agentService.getAgent(id));
    }

    @PostMapping("/push-command")
    @Operation(summary = "向指定 Agent 推送命令")
    @PermitAll
    public CommonResult<Boolean> pushCommand(@Validated @RequestBody AgentCommandReqVO reqVO) {
        agentService.pushCommand(reqVO);
        return success(true);
    }

    @PostMapping("/heartbeat")
    @Operation(summary = "接收 Agent 心跳上报")
    @PermitAll
    public CommonResult<Boolean> heartbeat(@RequestBody AgentHeartbeatReqVO reqVO) {
        agentService.receiveHeartbeat(reqVO);
        return success(true);
    }

    @PostMapping(path = "/content", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "连接 SSE 推送流", description = "Agent 建立长连接接收配置和命令")
    @PermitAll
    public SseEmitter content(@RequestHeader(value = "X-Agent-UUID", defaultValue = "unknown") String agentUuid,
                              @RequestBody AgentSaveReqVO reqVO) {

        agentService.createAgent(reqVO);
        SseEmitter emitter = new SseEmitter(120_000L); // 60 秒超时
        // 注册客户端
        agentService.registerClient(agentUuid, emitter);
        // 客户端断开时清理
        emitter.onCompletion(() -> agentService.removeClient(agentUuid));
        emitter.onTimeout(() -> agentService.removeClient(agentUuid));
        emitter.onError((e) -> agentService.removeClient(agentUuid));

        return emitter;
    }

}
