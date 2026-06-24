package com.huanniankj.module.analysis.controller;

import com.huanniankj.framework.common.pojo.CommonResult;
import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.analysis.controller.vo.FunnelConfigPageReqVO;
import com.huanniankj.module.analysis.controller.vo.FunnelConfigRespVO;
import com.huanniankj.module.analysis.controller.vo.FunnelConfigSaveReqVO;
import com.huanniankj.module.analysis.controller.vo.FunnelResultRespVO;
import com.huanniankj.module.analysis.service.FunnelConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static com.huanniankj.framework.common.pojo.CommonResult.success;

@Tag(name = "漏斗分析配置", description = "漏斗分析配置的增删改查")
@RestController
@RequestMapping("/analysis/funnel-config")
@Validated
public class FunnelConfigController {

    @Resource
    private FunnelConfigService funnelConfigService;

    @PostMapping("/create")
    @Operation(summary = "创建漏斗分析配置")
    @PermitAll
    public CommonResult<Long> createFunnelConfig(@Validated @RequestBody FunnelConfigSaveReqVO saveReqVO) {
        return success(funnelConfigService.createFunnelConfig(saveReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新漏斗分析配置")
    @PermitAll
    public CommonResult<Boolean> updateFunnelConfig(@Validated @RequestBody FunnelConfigSaveReqVO saveReqVO) {
        funnelConfigService.updateFunnelConfig(saveReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除漏斗分析配置")
    @Parameter(name = "id", description = "配置 ID", required = true)
    @PermitAll
    public CommonResult<Boolean> deleteFunnelConfig(@RequestParam("id") Long id) {
        funnelConfigService.deleteFunnelConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取漏斗分析配置详情")
    @Parameter(name = "id", description = "配置 ID", required = true)
    @PermitAll
    public CommonResult<FunnelConfigRespVO> getFunnelConfig(@RequestParam("id") Long id) {
        return success(funnelConfigService.getFunnelConfig(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获取漏斗分析配置分页")
    @PermitAll
    public CommonResult<PageResult<FunnelConfigRespVO>> getFunnelConfigPage(@Validated FunnelConfigPageReqVO pageReqVO) {
        return success(funnelConfigService.getFunnelConfigPage(pageReqVO));
    }

    @GetMapping("/results")
    @Operation(summary = "查询漏斗分析结果")
    @PermitAll
    public CommonResult<List<FunnelResultRespVO>> getFunnelResults(
            @RequestParam("configId") Long configId,
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return success(funnelConfigService.getFunnelResults(configId, startDate, endDate));
    }

}
