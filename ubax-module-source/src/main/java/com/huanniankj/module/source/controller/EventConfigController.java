package com.huanniankj.module.source.controller;

import com.huanniankj.framework.common.pojo.CommonResult;
import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.source.controller.agent.vo.EventConfigPageReqVO;
import com.huanniankj.module.source.controller.agent.vo.EventConfigRespVO;
import com.huanniankj.module.source.controller.agent.vo.EventConfigSaveReqVO;
import com.huanniankj.module.source.service.agent.EventConfigService;
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
 * 事件配置控制层
 *
 * @author zhaoff
 */
@Tag(name = "Agent 事件配置", description = "Agent 事件匹配规则配置接口")
@RestController("sourceEventConfigController")
@RequestMapping("/gather/event-config")
@Validated
public class EventConfigController {

    @Resource
    private EventConfigService eventConfigService;

    @PostMapping("/create")
    @Operation(summary = "创建事件配置", description = "用于创建事件匹配规则配置")
    @PreAuthorize("@ss.hasPermission('source:event-config:create')")
    public CommonResult<Long> createEventConfig(@Valid @RequestBody EventConfigSaveReqVO saveReqVO) {
        return success(eventConfigService.createEventConfig(saveReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新事件配置", description = "用于更新事件匹配规则配置")
    @PreAuthorize("@ss.hasPermission('source:event-config:update')")
    public CommonResult<Boolean> updateEventConfig(@Valid @RequestBody EventConfigSaveReqVO saveReqVO) {
        eventConfigService.updateEventConfig(saveReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除事件配置", description = "用于删除事件匹配规则配置")
    @Parameter(name = "id", description = "配置 ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('source:event-config:delete')")
    public CommonResult<Boolean> deleteEventConfig(@RequestParam("id") Long id) {
        eventConfigService.deleteEventConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得事件配置详情", description = "用于管理后台查看事件配置详情")
    @Parameter(name = "id", description = "配置 ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('source:event-config:query')")
    public CommonResult<EventConfigRespVO> getEventConfig(@RequestParam("id") Long id) {
        return success(eventConfigService.getEventConfig(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得事件配置分页列表", description = "用于管理后台查看事件配置列表")
    @PreAuthorize("@ss.hasPermission('source:event-config:query')")
    public CommonResult<PageResult<EventConfigRespVO>> getEventConfigPage(@Valid EventConfigPageReqVO pageReqVO) {
        return success(eventConfigService.getEventConfigPage(pageReqVO));
    }

    @GetMapping("/list-enabled")
    @Operation(summary = "获得所有启用的事件配置列表", description = "用于获取所有启用的事件匹配规则")
    @PermitAll
    public CommonResult<List<EventConfigRespVO>> getEnabledEventConfigList() {
        return success(eventConfigService.getEnabledEventConfigList());
    }

    @GetMapping("/list-by-position")
    @Operation(summary = "根据匹配位置获得启用的事件配置列表", description = "用于获取指定匹配位置的启用规则")
    @Parameter(name = "matchPosition", description = "匹配位置", required = true, example = "http_body")
    @PermitAll
    public CommonResult<List<EventConfigRespVO>> getEnabledEventConfigListByPosition(
            @RequestParam("matchPosition") String matchPosition) {
        return success(eventConfigService.getEnabledEventConfigListByPosition(matchPosition));
    }

    @PostMapping("/match")
    @Operation(summary = "匹配事件", description = "根据匹配规则匹配 Agent 上传的事件数据")
    @PermitAll
    public CommonResult<List<EventConfigRespVO>> matchEvent(
            @RequestParam("position") String position,
            @RequestParam("content") String content) {
        return success(eventConfigService.matchEvent(position, content));
    }

}
