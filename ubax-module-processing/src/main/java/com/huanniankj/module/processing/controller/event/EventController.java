package com.huanniankj.module.processing.controller.event;

import com.huanniankj.framework.common.pojo.CommonResult;
import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.common.util.object.BeanUtils;
import com.huanniankj.module.processing.controller.event.vo.EventPageReqVO;
import com.huanniankj.module.processing.controller.event.vo.EventRespVO;
import com.huanniankj.module.processing.controller.event.vo.EventSaveReqVO;
import com.huanniankj.module.processing.dal.dataobject.event.EventDO;
import com.huanniankj.module.processing.service.event.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.huanniankj.framework.common.pojo.CommonResult.success;

/**
 * 事件
 *
 * @author zhaoff
 */
@Tag(name = "事件")
@RestController
@RequestMapping("/processing/event")
@Validated
public class EventController {

    @Resource
    private EventService eventService;

    @PostMapping("/create")
    @Operation(summary = "创建事件")
    @PreAuthorize("@ss.hasPermission('processing:event:create')")
    public CommonResult<Long> createEvent(@Valid @RequestBody EventSaveReqVO createReqVO) {
        return success(eventService.createEvent(createReqVO));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除事件")
    @Parameter(name = "id", description = "事件 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('processing:event:delete')")
    public CommonResult<Boolean> deleteEvent(@RequestParam("id") Long id) {
        eventService.deleteEvent(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得事件")
    @Parameter(name = "id", description = "事件 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('processing:event:query')")
    public CommonResult<EventRespVO> getEvent(@RequestParam("id") Long id) {
        EventDO event = eventService.getEvent(id);
        return success(BeanUtils.toBean(event, EventRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得事件分页")
    @PreAuthorize("@ss.hasPermission('processing:event:query')")
    public CommonResult<PageResult<EventRespVO>> getEventPage(@Validated EventPageReqVO pageReqVO) {
        PageResult<EventDO> pageResult = eventService.getEventPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, EventRespVO.class));
    }

}
