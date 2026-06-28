package com.huanniankj.module.source.controller.agent;

import com.huanniankj.framework.common.pojo.CommonResult;
import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.source.controller.agent.vo.AgentLogHandleReqVO;
import com.huanniankj.module.source.controller.agent.vo.AgentLogPageReqVO;
import com.huanniankj.module.source.controller.agent.vo.AgentLogRespVO;
import com.huanniankj.module.source.controller.agent.vo.AgentLogSaveReqVO;
import com.huanniankj.module.source.service.agent.AgentLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.huanniankj.framework.common.pojo.CommonResult.success;

/**
 * 事件管理控制层
 *
 * @author zhaoff
 */
@Tag(name = "Agent 事件管理", description = "Agent 事件管理接口")
@RestController("sourceEventController")
@RequestMapping("/gather/event")
@Validated
public class AgentLogController {

    @Resource
    private AgentLogService agentLogService;

    @PostMapping("/create")
    @Operation(summary = "创建事件", description = "用于 Agent 上报事件或手动创建事件")
    @PermitAll
    public CommonResult<Long> createEvent(@Valid @RequestBody AgentLogSaveReqVO saveReqVO) {
        return success(agentLogService.createEvent(saveReqVO));
    }

    @PutMapping("/handle")
    @Operation(summary = "处理事件", description = "用于管理后台处理事件")
    @PreAuthorize("@ss.hasPermission('source:event:update')")
    public CommonResult<Boolean> handleEvent(@Valid @RequestBody AgentLogHandleReqVO handleReqVO) {
        agentLogService.handleEvent(handleReqVO);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得事件详情", description = "用于管理后台查看事件详情")
    @Parameter(name = "id", description = "事件 ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('source:event:query')")
    public CommonResult<AgentLogRespVO> getEvent(@RequestParam("id") Long id) {
        return success(agentLogService.getEvent(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得事件分页列表", description = "用于管理后台查看事件列表")
    @PreAuthorize("@ss.hasPermission('source:event:query')")
    public CommonResult<PageResult<AgentLogRespVO>> getEventPage(@Valid AgentLogPageReqVO pageReqVO) {
        return success(agentLogService.getEventPage(pageReqVO));
    }

    @GetMapping("/list-by-agent")
    @Operation(summary = "根据 Agent UUID 获得事件列表", description = "用于查看指定 Agent 的事件列表")
    @Parameter(name = "agentUuid", description = "Agent UUID", required = true, example = "agent-uuid-001")
    @PermitAll
    public CommonResult<List<AgentLogRespVO>> getEventListByAgentUuid(@RequestParam("agentUuid") String agentUuid) {
        return success(agentLogService.getEventListByAgentUuid(agentUuid));
    }

    @GetMapping("/count-by-agent-and-level")
    @Operation(summary = "根据 Agent UUID 和事件级别统计事件数量", description = "用于统计指定 Agent 指定级别的事件数量")
    @Parameter(name = "agentUuid", description = "Agent UUID", required = true, example = "agent-uuid-001")
    @Parameter(name = "eventLevel", description = "事件级别", required = true, example = "1")
    @PermitAll
    public CommonResult<Long> countEventByAgentUuidAndLevel(
            @RequestParam("agentUuid") String agentUuid,
            @RequestParam("eventLevel") Integer eventLevel) {
        return success(agentLogService.countEventByAgentUuidAndLevel(agentUuid, eventLevel));
    }

    @GetMapping("/count-by-agent-and-handled")
    @Operation(summary = "根据 Agent UUID 和处理状态统计事件数量", description = "用于统计指定 Agent 已处理/未处理的事件数量")
    @Parameter(name = "agentUuid", description = "Agent UUID", required = true, example = "agent-uuid-001")
    @Parameter(name = "handled", description = "是否已处理", required = true, example = "true")
    @PermitAll
    public CommonResult<Long> countEventByAgentUuidAndHandled(
            @RequestParam("agentUuid") String agentUuid,
            @RequestParam("handled") Boolean handled) {
        return success(agentLogService.countEventByAgentUuidAndHandled(agentUuid, handled));
    }

}
