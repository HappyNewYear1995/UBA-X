package com.huanniankj.module.analysis.service;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.analysis.controller.vo.RetentionConfigPageReqVO;
import com.huanniankj.module.analysis.controller.vo.RetentionConfigRespVO;
import com.huanniankj.module.analysis.controller.vo.RetentionConfigSaveReqVO;
import com.huanniankj.module.analysis.controller.vo.RetentionResultRespVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 留存分析配置服务接口
 *
 * @author zhaoff
 */
public interface RetentionConfigService {

    Long createRetentionConfig(RetentionConfigSaveReqVO saveReqVO);

    void updateRetentionConfig(RetentionConfigSaveReqVO saveReqVO);

    void deleteRetentionConfig(Long id);

    RetentionConfigRespVO getRetentionConfig(Long id);

    PageResult<RetentionConfigRespVO> getRetentionConfigPage(RetentionConfigPageReqVO pageReqVO);

    /**
     * 查询留存分析结果
     *
     * @param configId  配置 ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 留存结果列表
     */
    List<RetentionResultRespVO> getRetentionResults(Long configId, LocalDate startDate, LocalDate endDate);

}
