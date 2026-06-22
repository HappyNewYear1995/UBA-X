package com.huanniankj.module.analysis.controller;

import com.huanniankj.framework.common.pojo.CommonResult;
import com.huanniankj.module.analysis.controller.vo.PathAnalysisReqVO;
import com.huanniankj.module.analysis.controller.vo.PathAnalysisRespVO;
import com.huanniankj.module.analysis.service.PathAnalysisService;
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
 * 路径分析控制层
 *
 * @author zhaoff
 */
@Tag(name = "管理后台 - 路径分析", description = "用于分析用户在应用中的行为路径流转")
@RestController
@RequestMapping("/analysis/path")
@Validated
public class PathAnalysisController {

    @Resource
    private PathAnalysisService pathAnalysisService;

    @PostMapping("/analyze")
    @Operation(summary = "执行路径分析", description = "统计用户行为路径流转，生成桑基图和路径统计")
    @PermitAll
    public CommonResult<PathAnalysisRespVO> analyzePath(@RequestBody @Validated PathAnalysisReqVO reqVO) {
        return CommonResult.success(pathAnalysisService.analyzePath(reqVO));
    }
}
