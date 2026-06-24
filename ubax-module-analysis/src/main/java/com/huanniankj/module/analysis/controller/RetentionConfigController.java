package com.huanniankj.module.analysis.controller;

import com.huanniankj.framework.common.pojo.CommonResult;
import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.analysis.controller.vo.RetentionConfigPageReqVO;
import com.huanniankj.module.analysis.controller.vo.RetentionConfigRespVO;
import com.huanniankj.module.analysis.controller.vo.RetentionConfigSaveReqVO;
import com.huanniankj.module.analysis.controller.vo.RetentionResultRespVO;
import com.huanniankj.module.analysis.service.RetentionConfigService;
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

@Tag(name = "留存分析配置", description = "留存分析配置的增删改查及结果查询")
@RestController
@RequestMapping("/analysis/retention-config")
@Validated
public class RetentionConfigController {

    @Resource
    private RetentionConfigService retentionConfigService;

    @PostMapping("/create")
    @Operation(summary = "创建留存分析配置")
    @PermitAll
    public CommonResult<Long> createRetentionConfig(@Validated @RequestBody RetentionConfigSaveReqVO saveReqVO) {
        return success(retentionConfigService.createRetentionConfig(saveReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新留存分析配置")
    @PermitAll
    public CommonResult<Boolean> updateRetentionConfig(@Validated @RequestBody RetentionConfigSaveReqVO saveReqVO) {
        retentionConfigService.updateRetentionConfig(saveReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除留存分析配置")
    @Parameter(name = "id", description = "配置 ID", required = true)
    @PermitAll
    public CommonResult<Boolean> deleteRetentionConfig(@RequestParam("id") Long id) {
        retentionConfigService.deleteRetentionConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取留存分析配置详情")
    @Parameter(name = "id", description = "配置 ID", required = true)
    @PermitAll
    public CommonResult<RetentionConfigRespVO> getRetentionConfig(@RequestParam("id") Long id) {
        return success(retentionConfigService.getRetentionConfig(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获取留存分析配置分页")
    @PermitAll
    public CommonResult<PageResult<RetentionConfigRespVO>> getRetentionConfigPage(@Validated RetentionConfigPageReqVO pageReqVO) {
        return success(retentionConfigService.getRetentionConfigPage(pageReqVO));
    }

    @GetMapping("/results")
    @Operation(summary = "查询留存分析结果")
    @PermitAll
    public CommonResult<List<RetentionResultRespVO>> getRetentionResults(
            @RequestParam("configId") Long configId,
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return success(retentionConfigService.getRetentionResults(configId, startDate, endDate));
    }

}
