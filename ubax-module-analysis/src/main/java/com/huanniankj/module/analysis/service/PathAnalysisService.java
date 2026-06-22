package com.huanniankj.module.analysis.service;

import com.huanniankj.module.analysis.controller.vo.PathAnalysisReqVO;
import com.huanniankj.module.analysis.controller.vo.PathAnalysisRespVO;

/**
 * 路径分析服务接口
 *
 * @author zhaoff
 */
public interface PathAnalysisService {

    /**
     * 执行路径分析
     *
     * @param reqVO 请求参数
     * @return 路径分析结果
     */
    PathAnalysisRespVO analyzePath(PathAnalysisReqVO reqVO);
}
