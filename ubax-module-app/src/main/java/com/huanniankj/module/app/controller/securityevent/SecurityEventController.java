package com.huanniankj.module.app.controller.securityevent;

import com.huanniankj.framework.common.pojo.CommonResult;
import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.common.util.object.BeanUtils;
import com.huanniankj.module.app.controller.securityevent.vo.SecurityEventHandleReqVO;
import com.huanniankj.module.app.controller.securityevent.vo.SecurityEventPageReqVO;
import com.huanniankj.module.app.controller.securityevent.vo.SecurityEventRespVO;
import com.huanniankj.module.app.dal.dataobject.security.SecurityEventDO;
import com.huanniankj.module.app.service.security.SecurityEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.huanniankj.framework.common.pojo.CommonResult.success;

@Tag(name = "安全检测事件")
@RestController
@RequestMapping("/app/security-event")
@Validated
public class SecurityEventController {

    @Resource
    private SecurityEventService securityEventService;

    @GetMapping("/get")
    @Operation(summary = "获得安全检测事件")
    @Parameter(name = "id", description = "安全检测事件 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('app:security-event:query')")
    public CommonResult<SecurityEventRespVO> getSecurityEvent(@RequestParam("id") Long id) {
        SecurityEventDO securityEvent = securityEventService.getSecurityEvent(id);
        return success(BeanUtils.toBean(securityEvent, SecurityEventRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得安全检测事件分页")
    @PreAuthorize("@ss.hasPermission('app:security-event:query')")
    public CommonResult<PageResult<SecurityEventRespVO>> getSecurityEventPage(@Valid SecurityEventPageReqVO pageReqVO) {
        PageResult<SecurityEventDO> pageResult = securityEventService.getSecurityEventPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SecurityEventRespVO.class));
    }

    @PutMapping("/handle")
    @Operation(summary = "处理安全检测事件")
    @PreAuthorize("@ss.hasPermission('app:security-event:handle')")
    public CommonResult<Boolean> handleSecurityEvent(@Valid @RequestBody SecurityEventHandleReqVO handleReqVO) {
        securityEventService.handleSecurityEvent(handleReqVO);
        return success(true);
    }

}
