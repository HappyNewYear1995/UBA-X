package com.huanniankj.module.analysis.service;

import com.huanniankj.module.analysis.controller.vo.RetentionAnalysisReqVO;
import com.huanniankj.module.analysis.controller.vo.RetentionAnalysisRespVO;

/**
 * 留存分析服务接口
 *
 * @author zhaoff
 */
public interface RetentionAnalysisService {

    /**
     * 执行留存分析
     *
     * @param reqVO 请求参数
     * @return 留存分析结果
     */
    RetentionAnalysisRespVO analyzeRetention(RetentionAnalysisReqVO reqVO);
}
