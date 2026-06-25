package com.huanniankj.module.app.controller.dashboard;

import com.huanniankj.framework.common.pojo.CommonResult;
import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.common.util.object.BeanUtils;
import com.huanniankj.module.app.controller.dashboard.vo.DashboardPageReqVO;
import com.huanniankj.module.app.controller.dashboard.vo.DashboardRespVO;
import com.huanniankj.module.app.controller.dashboard.vo.DashboardSaveReqVO;
import com.huanniankj.module.app.dal.dataobject.dashboard.DashboardDO;
import com.huanniankj.module.app.service.dashboard.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.huanniankj.framework.common.pojo.CommonResult.success;

@Tag(name = "数据看板")
@RestController
@RequestMapping("/app/dashboard")
@Validated
public class DashboardController {

    @Resource
    private DashboardService dashboardService;

    @PostMapping("/create")
    @Operation(summary = "创建数据看板")
    @PreAuthorize("@ss.hasPermission('app:dashboard:create')")
    public CommonResult<Long> createDashboard(@Valid @RequestBody DashboardSaveReqVO createReqVO) {
        return success(dashboardService.createDashboard(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新数据看板")
    @PreAuthorize("@ss.hasPermission('app:dashboard:update')")
    public CommonResult<Boolean> updateDashboard(@Valid @RequestBody DashboardSaveReqVO updateReqVO) {
        dashboardService.updateDashboard(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除数据看板")
    @Parameter(name = "id", description = "数据看板 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('app:dashboard:delete')")
    public CommonResult<Boolean> deleteDashboard(@RequestParam("id") Long id) {
        dashboardService.deleteDashboard(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得数据看板")
    @Parameter(name = "id", description = "数据看板 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('app:dashboard:query')")
    public CommonResult<DashboardRespVO> getDashboard(@RequestParam("id") Long id) {
        DashboardDO dashboard = dashboardService.getDashboard(id);
        return success(BeanUtils.toBean(dashboard, DashboardRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得数据看板分页")
    @PreAuthorize("@ss.hasPermission('app:dashboard:query')")
    public CommonResult<PageResult<DashboardRespVO>> getDashboardPage(@Valid DashboardPageReqVO pageReqVO) {
        PageResult<DashboardDO> pageResult = dashboardService.getDashboardPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, DashboardRespVO.class));
    }

}
