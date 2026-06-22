package com.huanniankj.module.analysis.controller;

import com.huanniankj.framework.common.pojo.CommonResult;
import com.huanniankj.module.analysis.controller.vo.EventAnalysisReqVO;
import com.huanniankj.module.analysis.controller.vo.EventAnalysisRespVO;
import com.huanniankj.module.analysis.service.EventAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "管理后台 - 数据分析大盘", description = "用于展示基于 ClickHouse 计算的用户行为、机器监控大盘数据")
@RestController
@RequestMapping("/analysis/dashboard")
@Validated
public class EventAnalysisController {

    @Resource
    private EventAnalysisService eventAnalysisService;

    @PostMapping("/trend")
    @Operation(summary = "查询事件趋势 (PV/UV/平均耗时等)", description = "支持按小时、省份、浏览器等维度聚合")
    @PermitAll
    public CommonResult<List<EventAnalysisRespVO>> getEventTrend(@RequestBody @Validated EventAnalysisReqVO reqVO) {
        List<EventAnalysisRespVO> list = eventAnalysisService.analyzeEvent(reqVO);
        return CommonResult.success(list);
    }
}
