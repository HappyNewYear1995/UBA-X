package com.huanniankj.module.gather.controller.agent;

import com.huanniankj.framework.common.pojo.CommonResult;
import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.gather.controller.agent.vo.*;
import com.huanniankj.module.gather.controller.agent.vo.*;
import com.huanniankj.module.gather.service.agent.AgentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.huanniankj.framework.common.pojo.CommonResult.success;

/**
 * Agent 控制层
 *
 * @author zhaoff
 */
@Slf4j
@Tag(name = "Agent 管理", description = "Agent 探针管理接口")
@RestController
@RequestMapping("/gather/agent")
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

    @PostMapping("/push-config")
    @Operation(summary = "向指定 Agent 推送配置")
    @Parameter(name = "uuid", description = "Agent UUID", required = true, example = "agent-uuid-001")
    @PermitAll
    public CommonResult<Boolean> pushConfig(@RequestParam("uuid") String uuid) {
        agentService.pushConfig(uuid);
        return success(true);
    }

    @PostMapping("/register")
    @Operation(summary = "Agent 注册", description = "客户端启动时调用，注册 Agent 信息并上送操作系统、版本等")
    @PermitAll
    public CommonResult<Boolean> register(@Validated @RequestBody AgentSaveReqVO reqVO) {
        agentService.createAgent(reqVO);
        return success(true);
    }

    @PostMapping("/heartbeat")
    @Operation(summary = "接收 Agent 心跳上报")
    @PermitAll
    public CommonResult<Boolean> heartbeat(@RequestBody AgentHeartbeatReqVO reqVO) {
        agentService.receiveHeartbeat(reqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新 Agent 状态", description = "用于开启或关闭指定 Agent")
    @Parameter(name = "id", description = "Agent ID", required = true, example = "1")
    @Parameter(name = "status", description = "状态值 0-开启 1-关闭", required = true, example = "0")
    @PermitAll
    public CommonResult<Boolean> updateAgentStatus(@RequestParam("id") Long id, @RequestParam("status") Integer status) {
        agentService.updateAgentStatus(id, status);
        return success(true);
    }

    @PutMapping("/update")
    @Operation(summary = "更新 Agent 信息", description = "用于编辑 Agent 平台类型、备注和配置信息")
    @PermitAll
    public CommonResult<Boolean> updateAgent(@Validated @RequestBody AgentUpdateReqVO updateReqVO) {
        agentService.updateAgent(updateReqVO);
        return success(true);
    }

}
