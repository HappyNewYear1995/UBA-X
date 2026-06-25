package com.huanniankj.module.app.controller.securityrule;

import com.huanniankj.framework.common.pojo.CommonResult;
import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.common.util.object.BeanUtils;
import com.huanniankj.module.app.controller.securityrule.vo.SecurityRulePageReqVO;
import com.huanniankj.module.app.controller.securityrule.vo.SecurityRuleRespVO;
import com.huanniankj.module.app.controller.securityrule.vo.SecurityRuleSaveReqVO;
import com.huanniankj.module.app.dal.dataobject.security.SecurityRuleDO;
import com.huanniankj.module.app.service.security.SecurityRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.huanniankj.framework.common.pojo.CommonResult.success;

@Tag(name = "安全检测规则")
@RestController
@RequestMapping("/app/security-rule")
@Validated
public class SecurityRuleController {

    @Resource
    private SecurityRuleService securityRuleService;

    @PostMapping("/create")
    @Operation(summary = "创建安全检测规则")
    @PreAuthorize("@ss.hasPermission('app:security-rule:create')")
    public CommonResult<Long> createSecurityRule(@Valid @RequestBody SecurityRuleSaveReqVO createReqVO) {
        return success(securityRuleService.createSecurityRule(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新安全检测规则")
    @PreAuthorize("@ss.hasPermission('app:security-rule:update')")
    public CommonResult<Boolean> updateSecurityRule(@Valid @RequestBody SecurityRuleSaveReqVO updateReqVO) {
        securityRuleService.updateSecurityRule(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除安全检测规则")
    @Parameter(name = "id", description = "安全检测规则 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('app:security-rule:delete')")
    public CommonResult<Boolean> deleteSecurityRule(@RequestParam("id") Long id) {
        securityRuleService.deleteSecurityRule(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得安全检测规则")
    @Parameter(name = "id", description = "安全检测规则 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('app:security-rule:query')")
    public CommonResult<SecurityRuleRespVO> getSecurityRule(@RequestParam("id") Long id) {
        SecurityRuleDO securityRule = securityRuleService.getSecurityRule(id);
        return success(BeanUtils.toBean(securityRule, SecurityRuleRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得安全检测规则分页")
    @PreAuthorize("@ss.hasPermission('app:security-rule:query')")
    public CommonResult<PageResult<SecurityRuleRespVO>> getSecurityRulePage(@Valid SecurityRulePageReqVO pageReqVO) {
        PageResult<SecurityRuleDO> pageResult = securityRuleService.getSecurityRulePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SecurityRuleRespVO.class));
    }

    @PutMapping("/update-enabled")
    @Operation(summary = "更新安全检测规则启用状态")
    @Parameter(name = "id", description = "安全检测规则 ID", required = true, example = "1024")
    @Parameter(name = "enabled", description = "是否启用", required = true, example = "true")
    @PreAuthorize("@ss.hasPermission('app:security-rule:update')")
    public CommonResult<Boolean> updateSecurityRuleEnabled(@RequestParam("id") Long id,
                                                           @RequestParam("enabled") Boolean enabled) {
        securityRuleService.updateSecurityRuleEnabled(id, enabled);
        return success(true);
    }

}
