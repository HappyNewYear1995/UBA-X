package com.huanniankj.module.gather.controller.event;

import com.huanniankj.framework.common.pojo.CommonResult;
import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.gather.controller.event.vo.EventHandleReqVO;
import com.huanniankj.module.gather.controller.event.vo.EventPageReqVO;
import com.huanniankj.module.gather.controller.event.vo.EventRespVO;
import com.huanniankj.module.gather.controller.event.vo.EventSaveReqVO;
import com.huanniankj.module.gather.service.event.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.huanniankj.framework.common.pojo.CommonResult.success;

/**
 * 事件管理控制层
 *
 * @author zhaoff
 */
@Tag(name = "管理后台 - 事件管理", description = "Agent 事件管理接口")
@RestController
@RequestMapping("/gather/event")
@Validated
public class EventController {

    @Resource
    private EventService eventService;

    @PostMapping("/create")
    @Operation(summary = "创建事件", description = "用于 Agent 上报事件或手动创建事件")
    @PermitAll
    public CommonResult<Long> createEvent(@Valid @RequestBody EventSaveReqVO saveReqVO) {
        return success(eventService.createEvent(saveReqVO));
    }

    @PutMapping("/handle")
    @Operation(summary = "处理事件", description = "用于管理后台处理事件")
    @PermitAll
    public CommonResult<Boolean> handleEvent(@Valid @RequestBody EventHandleReqVO handleReqVO) {
        eventService.handleEvent(handleReqVO);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得事件详情", description = "用于管理后台查看事件详情")
    @Parameter(name = "id", description = "事件 ID", required = true, example = "1")
    @PermitAll
    public CommonResult<EventRespVO> getEvent(@RequestParam("id") Long id) {
        return success(eventService.getEvent(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得事件分页列表", description = "用于管理后台查看事件列表")
    @PermitAll
    public CommonResult<PageResult<EventRespVO>> getEventPage(@Valid EventPageReqVO pageReqVO) {
        return success(eventService.getEventPage(pageReqVO));
    }

    @GetMapping("/list-by-agent")
    @Operation(summary = "根据 Agent UUID 获得事件列表", description = "用于查看指定 Agent 的事件列表")
    @Parameter(name = "agentUuid", description = "Agent UUID", required = true, example = "agent-uuid-001")
    @PermitAll
    public CommonResult<List<EventRespVO>> getEventListByAgentUuid(@RequestParam("agentUuid") String agentUuid) {
        return success(eventService.getEventListByAgentUuid(agentUuid));
    }

    @GetMapping("/count-by-agent-and-level")
    @Operation(summary = "根据 Agent UUID 和事件级别统计事件数量", description = "用于统计指定 Agent 指定级别的事件数量")
    @Parameter(name = "agentUuid", description = "Agent UUID", required = true, example = "agent-uuid-001")
    @Parameter(name = "eventLevel", description = "事件级别", required = true, example = "1")
    @PermitAll
    public CommonResult<Long> countEventByAgentUuidAndLevel(
            @RequestParam("agentUuid") String agentUuid,
            @RequestParam("eventLevel") Integer eventLevel) {
        return success(eventService.countEventByAgentUuidAndLevel(agentUuid, eventLevel));
    }

    @GetMapping("/count-by-agent-and-handled")
    @Operation(summary = "根据 Agent UUID 和处理状态统计事件数量", description = "用于统计指定 Agent 已处理/未处理的事件数量")
    @Parameter(name = "agentUuid", description = "Agent UUID", required = true, example = "agent-uuid-001")
    @Parameter(name = "handled", description = "是否已处理", required = true, example = "true")
    @PermitAll
    public CommonResult<Long> countEventByAgentUuidAndHandled(
            @RequestParam("agentUuid") String agentUuid,
            @RequestParam("handled") Boolean handled) {
        return success(eventService.countEventByAgentUuidAndHandled(agentUuid, handled));
    }

}
