package com.huanniankj.module.analysis.controller;

import com.huanniankj.framework.common.pojo.CommonResult;
import com.huanniankj.module.analysis.controller.vo.RetentionAnalysisReqVO;
import com.huanniankj.module.analysis.controller.vo.RetentionAnalysisRespVO;
import com.huanniankj.module.analysis.service.RetentionAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 留存分析控制层
 *
 * @author zhaoff
 */
@Tag(name = "留存分析", description = "用于分析用户在后续时间的留存情况")
@RestController
@RequestMapping("/analysis/retention")
@Validated
public class RetentionAnalysisController {

    @Resource
    private RetentionAnalysisService retentionAnalysisService;

    @PostMapping("/analyze")
    @Operation(summary = "执行留存分析", description = "按日期统计新用户数和后续留存率")
    @PermitAll
    public CommonResult<RetentionAnalysisRespVO> analyzeRetention(@RequestBody @Validated RetentionAnalysisReqVO reqVO) {
        return CommonResult.success(retentionAnalysisService.analyzeRetention(reqVO));
    }
}
