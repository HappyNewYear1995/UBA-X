package com.huanniankj.module.processing.controller;

import com.huanniankj.framework.common.pojo.CommonResult;
import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.common.util.object.BeanUtils;
import com.huanniankj.module.processing.controller.vo.ErrorLogPageReqVO;
import com.huanniankj.module.processing.controller.vo.ErrorLogRespVO;
import com.huanniankj.module.processing.controller.vo.ErrorLogSaveReqVO;
import com.huanniankj.module.processing.dal.dataobject.ErrorLogDO;
import com.huanniankj.module.processing.service.ErrorLogService;
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
 * 异常日志
 *
 * @author zhaoff
 */
@Tag(name = "异常日志")
@RestController
@RequestMapping("/collect/error-log")
@Validated
public class ErrorLogController {

    @Resource
    private ErrorLogService errorLogService;

    @PostMapping("/create")
    @Operation(summary = "创建异常日志")
    @PreAuthorize("@ss.hasPermission('collect:error-log:create')")
    public CommonResult<Long> createErrorLog(@Valid @RequestBody ErrorLogSaveReqVO createReqVO) {
        return success(errorLogService.createErrorLog(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新异常日志")
    @PreAuthorize("@ss.hasPermission('collect:error-log:update')")
    public CommonResult<Boolean> updateErrorLog(@Valid @RequestBody ErrorLogSaveReqVO updateReqVO) {
        errorLogService.updateErrorLog(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除异常日志")
    @Parameter(name = "id", description = "日志 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('collect:error-log:delete')")
    public CommonResult<Boolean> deleteErrorLog(@RequestParam("id") Long id) {
        errorLogService.deleteErrorLog(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得异常日志")
    @Parameter(name = "id", description = "日志 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('collect:error-log:query')")
    public CommonResult<ErrorLogRespVO> getErrorLog(@RequestParam("id") Long id) {
        ErrorLogDO errorLog = errorLogService.getErrorLog(id);
        return success(BeanUtils.toBean(errorLog, ErrorLogRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得异常日志分页")
    @PreAuthorize("@ss.hasPermission('collect:error-log:query')")
    public CommonResult<PageResult<ErrorLogRespVO>> getErrorLogPage(@Validated ErrorLogPageReqVO pageReqVO) {
        PageResult<ErrorLogDO> pageResult = errorLogService.getErrorLogPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ErrorLogRespVO.class));
    }

}
