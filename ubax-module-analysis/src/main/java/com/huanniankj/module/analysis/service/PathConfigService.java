package com.huanniankj.module.analysis.service;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.analysis.controller.admin.vo.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 路径分析配置服务接口
 *
 * @author zhaoff
 */
public interface PathConfigService {

    Long createPathConfig(PathConfigSaveReqVO saveReqVO);

    void updatePathConfig(PathConfigSaveReqVO saveReqVO);

    void deletePathConfig(Long id);

    PathConfigRespVO getPathConfig(Long id);

    PageResult<PathConfigRespVO> getPathConfigPage(PathConfigPageReqVO pageReqVO);

    /**
     * 查询路径分析结果
     *
     * @param configId  配置 ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 路径结果列表
     */
    List<PathResultRespVO> getPathResults(Long configId, LocalDate startDate, LocalDate endDate);

}
