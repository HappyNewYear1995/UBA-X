package com.huanniankj.module.processing.controller.log;

import com.huanniankj.framework.common.pojo.CommonResult;
import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.common.util.object.BeanUtils;
import com.huanniankj.module.processing.controller.log.vo.DataLogPageReqVO;
import com.huanniankj.module.processing.controller.log.vo.DataLogRespVO;
import com.huanniankj.module.processing.controller.log.vo.DataLogSaveReqVO;
import com.huanniankj.module.processing.dal.dataobject.log.DataLogDO;
import com.huanniankj.module.processing.service.log.DataLogService;
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
 * 数据日志
 *
 * @author zhaoff
 */
@Tag(name = "数据日志")
@RestController
@RequestMapping("/processing/data-log")
@Validated
public class DataLogController {

    @Resource
    private DataLogService dataLogService;

    @PostMapping("/create")
    @Operation(summary = "创建数据日志")
    @PreAuthorize("@ss.hasPermission('collect:data-log:create')")
    public CommonResult<Long> createDataLog(@Valid @RequestBody DataLogSaveReqVO createReqVO) {
        return success(dataLogService.createDataLog(createReqVO));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除数据日志")
    @Parameter(name = "id", description = "日志 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('collect:data-log:delete')")
    public CommonResult<Boolean> deleteDataLog(@RequestParam("id") Long id) {
        dataLogService.deleteDataLog(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得数据日志")
    @Parameter(name = "id", description = "日志 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('collect:data-log:query')")
    public CommonResult<DataLogRespVO> getDataLog(@RequestParam("id") Long id) {
        DataLogDO dataLog = dataLogService.getDataLog(id);
        return success(BeanUtils.toBean(dataLog, DataLogRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得数据日志分页")
    @PreAuthorize("@ss.hasPermission('collect:data-log:query')")
    public CommonResult<PageResult<DataLogRespVO>> getDataLogPage(@Validated DataLogPageReqVO pageReqVO) {
        PageResult<DataLogDO> pageResult = dataLogService.getDataLogPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, DataLogRespVO.class));
    }

}
