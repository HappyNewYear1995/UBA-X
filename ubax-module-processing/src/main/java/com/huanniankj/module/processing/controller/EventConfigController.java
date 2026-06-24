package com.huanniankj.module.processing.controller;

import com.huanniankj.framework.common.pojo.CommonResult;
import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.common.util.object.BeanUtils;
import com.huanniankj.module.processing.controller.vo.EventConfigPageReqVO;
import com.huanniankj.module.processing.controller.vo.EventConfigRespVO;
import com.huanniankj.module.processing.controller.vo.EventConfigSaveReqVO;
import com.huanniankj.module.processing.dal.dataobject.EventConfigDO;
import com.huanniankj.module.processing.service.EventConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.huanniankj.framework.common.pojo.CommonResult.success;

/**
 * 事件配置
 *
 * @author zhaoff
 */
@Tag(name = "事件配置")
@RestController
@RequestMapping("/collect/event-config")
@Validated
public class EventConfigController {

    @Resource
    private EventConfigService eventConfigService;

    @PostMapping("/create")
    @Operation(summary = "创建事件配置")
    @PreAuthorize("@ss.hasPermission('collect:event-config:create')")
    public CommonResult<Long> createEventConfig(@Valid @RequestBody EventConfigSaveReqVO createReqVO) {
        return success(eventConfigService.createEventConfig(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新事件配置")
    @PreAuthorize("@ss.hasPermission('collect:event-config:update')")
    public CommonResult<Boolean> updateEventConfig(@Valid @RequestBody EventConfigSaveReqVO updateReqVO) {
        eventConfigService.updateEventConfig(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除事件配置")
    @Parameter(name = "id", description = "事件配置 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('collect:event-config:delete')")
    public CommonResult<Boolean> deleteEventConfig(@RequestParam("id") Long id) {
        eventConfigService.deleteEventConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得事件配置")
    @Parameter(name = "id", description = "事件配置 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('collect:event-config:query')")
    public CommonResult<EventConfigRespVO> getEventConfig(@RequestParam("id") Long id) {
        EventConfigDO eventConfig = eventConfigService.getEventConfig(id);
        return success(BeanUtils.toBean(eventConfig, EventConfigRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得事件配置分页")
    @PreAuthorize("@ss.hasPermission('collect:event-config:query')")
    public CommonResult<PageResult<EventConfigRespVO>> getEventConfigPage(@Validated EventConfigPageReqVO pageReqVO) {
        PageResult<EventConfigDO> pageResult = eventConfigService.getEventConfigPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, EventConfigRespVO.class));
    }

    @GetMapping("/list-all-simple")
    @Operation(summary = "获取所有启用的事件配置精简列表", description = "主要用于前端的下拉选项")
    public CommonResult<List<EventConfigRespVO>> getSimpleEventConfigList() {
        List<EventConfigDO> list = eventConfigService.getEventConfigList();
        return success(BeanUtils.toBean(list, EventConfigRespVO.class));
    }

}
