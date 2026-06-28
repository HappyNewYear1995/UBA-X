package com.huanniankj.module.analysis.controller.base;

import com.huanniankj.framework.common.pojo.CommonResult;
import com.huanniankj.module.analysis.controller.base.vo.EventAnalysisReqVO;
import com.huanniankj.module.analysis.controller.base.vo.EventAnalysisRespVO;
import com.huanniankj.module.analysis.service.base.EventAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author zhaoff
 */
@Tag(name = "数据分析大盘", description = "用于展示基于 ClickHouse 计算的用户行为、机器监控大盘数据")
@RestController
@RequestMapping("/analysis/dashboard")
@Validated
public class EventAnalysisController {

    @Resource
    private EventAnalysisService eventAnalysisService;

    @PostMapping("/trend")
    @Operation(summary = "查询事件趋势 (PV/UV/平均耗时等)", description = "支持按小时、省份、浏览器等维度聚合")
    @PreAuthorize("@ss.hasPermission('analysis:dashboard:query')")
    public CommonResult<List<EventAnalysisRespVO>> getEventTrend(@RequestBody @Validated EventAnalysisReqVO reqVO) {
        List<EventAnalysisRespVO> list = eventAnalysisService.analyzeEvent(reqVO);
        return CommonResult.success(list);
    }
}
