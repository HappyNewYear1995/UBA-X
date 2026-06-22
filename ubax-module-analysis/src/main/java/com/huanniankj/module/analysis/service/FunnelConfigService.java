package com.huanniankj.module.analysis.service;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.analysis.controller.admin.vo.FunnelConfigPageReqVO;
import com.huanniankj.module.analysis.controller.admin.vo.FunnelConfigRespVO;
import com.huanniankj.module.analysis.controller.admin.vo.FunnelConfigSaveReqVO;
import com.huanniankj.module.analysis.controller.admin.vo.FunnelResultRespVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 漏斗分析配置服务接口
 *
 * @author zhaoff
 */
public interface FunnelConfigService {

    Long createFunnelConfig(FunnelConfigSaveReqVO saveReqVO);

    void updateFunnelConfig(FunnelConfigSaveReqVO saveReqVO);

    void deleteFunnelConfig(Long id);

    FunnelConfigRespVO getFunnelConfig(Long id);

    PageResult<FunnelConfigRespVO> getFunnelConfigPage(FunnelConfigPageReqVO pageReqVO);

    /**
     * 查询漏斗分析结果
     *
     * @param configId  配置 ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 漏斗结果列表
     */
    List<FunnelResultRespVO> getFunnelResults(Long configId, LocalDate startDate, LocalDate endDate);

}
