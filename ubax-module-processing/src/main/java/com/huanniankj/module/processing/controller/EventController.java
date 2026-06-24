package com.huanniankj.module.processing.controller;

import com.huanniankj.framework.common.pojo.CommonResult;
import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.common.util.object.BeanUtils;
import com.huanniankj.module.processing.controller.vo.EventPageReqVO;
import com.huanniankj.module.processing.controller.vo.EventRespVO;
import com.huanniankj.module.processing.controller.vo.EventSaveReqVO;
import com.huanniankj.module.processing.dal.dataobject.EventDO;
import com.huanniankj.module.processing.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.huanniankj.framework.common.pojo.CommonResult.success;

/**
 * 事件
 * <p>
 * 事件为不可变对象（Immutable），不提供更新接口，仅支持创建、删除和查询。
 *
 * @author zhaoff
 */
@Tag(name = "事件")
@RestController
@RequestMapping("/collect/event")
@Validated
public class EventController {

    @Resource
    private EventService eventService;

    @PostMapping("/create")
    @Operation(summary = "创建事件")
    @PreAuthorize("@ss.hasPermission('collect:event:create')")
    public CommonResult<Long> createEvent(@Valid @RequestBody EventSaveReqVO createReqVO) {
        return success(eventService.createEvent(createReqVO));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除事件")
    @Parameter(name = "id", description = "事件 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('collect:event:delete')")
    public CommonResult<Boolean> deleteEvent(@RequestParam("id") Long id) {
        eventService.deleteEvent(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得事件")
    @Parameter(name = "id", description = "事件 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('collect:event:query')")
    public CommonResult<EventRespVO> getEvent(@RequestParam("id") Long id) {
        EventDO event = eventService.getEvent(id);
        return success(BeanUtils.toBean(event, EventRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得事件分页")
    @PreAuthorize("@ss.hasPermission('collect:event:query')")
    public CommonResult<PageResult<EventRespVO>> getEventPage(@Validated EventPageReqVO pageReqVO) {
        PageResult<EventDO> pageResult = eventService.getEventPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, EventRespVO.class));
    }

}
