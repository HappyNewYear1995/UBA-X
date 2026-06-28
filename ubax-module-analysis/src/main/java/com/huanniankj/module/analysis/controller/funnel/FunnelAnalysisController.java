package com.huanniankj.module.analysis.controller.funnel;

import com.huanniankj.framework.common.pojo.CommonResult;
import com.huanniankj.module.analysis.controller.funnel.vo.FunnelAnalysisReqVO;
import com.huanniankj.module.analysis.controller.funnel.vo.FunnelAnalysisRespVO;
import com.huanniankj.module.analysis.service.funnel.FunnelAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 漏斗分析控制层
 *
 * @author zhaoff
 */
@Tag(name = "漏斗分析", description = "用于分析用户在多步骤流程中的转化和流失情况")
@RestController
@RequestMapping("/analysis/funnel")
@Validated
public class FunnelAnalysisController {

    @Resource
    private FunnelAnalysisService funnelAnalysisService;

    @PostMapping("/analyze")
    @Operation(summary = "执行漏斗分析", description = "按步骤统计用户转化率、流失率等指标")
    @PreAuthorize("@ss.hasPermission('analysis:funnel:query')")
    public CommonResult<FunnelAnalysisRespVO> analyzeFunnel(@RequestBody @Validated FunnelAnalysisReqVO reqVO) {
        return CommonResult.success(funnelAnalysisService.analyzeFunnel(reqVO));
    }
}
