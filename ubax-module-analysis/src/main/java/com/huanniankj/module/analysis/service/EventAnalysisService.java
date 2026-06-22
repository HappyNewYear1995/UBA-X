package com.huanniankj.module.analysis.service;

import com.huanniankj.module.analysis.controller.vo.EventAnalysisReqVO;
import com.huanniankj.module.analysis.controller.vo.EventAnalysisRespVO;

import java.util.List;

public interface EventAnalysisService {

    /**
     * 基础事件分析：根据指定维度(groupBy)计算 PV、UV、平均耗时等指标
     *
     * @param reqVO 请求参数
     * @return 聚合后的指标列表
     */
    List<EventAnalysisRespVO> analyzeEvent(EventAnalysisReqVO reqVO);
}
