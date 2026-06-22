package com.huanniankj.module.analysis.service;

import com.huanniankj.module.analysis.controller.vo.FunnelAnalysisReqVO;
import com.huanniankj.module.analysis.controller.vo.FunnelAnalysisRespVO;

/**
 * 漏斗分析服务接口
 *
 * @author zhaoff
 */
public interface FunnelAnalysisService {

    /**
     * 执行漏斗分析
     *
     * @param reqVO 请求参数
     * @return 漏斗分析结果
     */
    FunnelAnalysisRespVO analyzeFunnel(FunnelAnalysisReqVO reqVO);
}
