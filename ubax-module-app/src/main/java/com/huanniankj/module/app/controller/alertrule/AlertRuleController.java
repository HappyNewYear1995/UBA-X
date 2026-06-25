package com.huanniankj.module.app.controller.alertrule;

import com.huanniankj.framework.common.pojo.CommonResult;
import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.common.util.object.BeanUtils;
import com.huanniankj.module.app.controller.alertrule.vo.AlertRulePageReqVO;
import com.huanniankj.module.app.controller.alertrule.vo.AlertRuleRespVO;
import com.huanniankj.module.app.controller.alertrule.vo.AlertRuleSaveReqVO;
import com.huanniankj.module.app.dal.dataobject.alert.AlertRuleDO;
import com.huanniankj.module.app.service.alert.AlertRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.huanniankj.framework.common.pojo.CommonResult.success;

@Tag(name = "告警规则")
@RestController
@RequestMapping("/app/alert-rule")
@Validated
public class AlertRuleController {

    @Resource
    private AlertRuleService alertRuleService;

    @PostMapping("/create")
    @Operation(summary = "创建告警规则")
    @PreAuthorize("@ss.hasPermission('app:alert-rule:create')")
    public CommonResult<Long> createAlertRule(@Valid @RequestBody AlertRuleSaveReqVO createReqVO) {
        return success(alertRuleService.createAlertRule(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新告警规则")
    @PreAuthorize("@ss.hasPermission('app:alert-rule:update')")
    public CommonResult<Boolean> updateAlertRule(@Valid @RequestBody AlertRuleSaveReqVO updateReqVO) {
        alertRuleService.updateAlertRule(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除告警规则")
    @Parameter(name = "id", description = "告警规则 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('app:alert-rule:delete')")
    public CommonResult<Boolean> deleteAlertRule(@RequestParam("id") Long id) {
        alertRuleService.deleteAlertRule(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得告警规则")
    @Parameter(name = "id", description = "告警规则 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('app:alert-rule:query')")
    public CommonResult<AlertRuleRespVO> getAlertRule(@RequestParam("id") Long id) {
        AlertRuleDO alertRule = alertRuleService.getAlertRule(id);
        return success(BeanUtils.toBean(alertRule, AlertRuleRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得告警规则分页")
    @PreAuthorize("@ss.hasPermission('app:alert-rule:query')")
    public CommonResult<PageResult<AlertRuleRespVO>> getAlertRulePage(@Valid AlertRulePageReqVO pageReqVO) {
        PageResult<AlertRuleDO> pageResult = alertRuleService.getAlertRulePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AlertRuleRespVO.class));
    }

    @PutMapping("/update-enabled")
    @Operation(summary = "更新告警规则启用状态")
    @Parameter(name = "id", description = "告警规则 ID", required = true, example = "1024")
    @Parameter(name = "enabled", description = "是否启用", required = true, example = "true")
    @PreAuthorize("@ss.hasPermission('app:alert-rule:update')")
    public CommonResult<Boolean> updateAlertRuleEnabled(@RequestParam("id") Long id,
                                                        @RequestParam("enabled") Boolean enabled) {
        alertRuleService.updateAlertRuleEnabled(id, enabled);
        return success(true);
    }

}
