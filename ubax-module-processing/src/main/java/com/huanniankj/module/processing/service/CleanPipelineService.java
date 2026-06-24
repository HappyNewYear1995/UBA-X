package com.huanniankj.module.processing.service;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.processing.controller.vo.CleanPipelinePageReqVO;
import com.huanniankj.module.processing.controller.vo.CleanPipelineSaveReqVO;
import com.huanniankj.module.processing.dal.dataobject.CleanPipelineDO;

import java.util.List;

/**
 * 清洗管道服务接口
 *
 * @author zhaoff
 */
public interface CleanPipelineService {

    /**
     * 创建清洗管道
     *
     * @param createReqVO 创建信息
     * @return 管道 ID
     */
    Long createCleanPipeline(CleanPipelineSaveReqVO createReqVO);

    /**
     * 更新清洗管道
     *
     * @param updateReqVO 更新信息
     */
    void updateCleanPipeline(CleanPipelineSaveReqVO updateReqVO);

    /**
     * 删除清洗管道
     *
     * @param id 管道 ID
     */
    void deleteCleanPipeline(Long id);

    /**
     * 获得清洗管道
     *
     * @param id 管道 ID
     * @return 清洗管道
     */
    CleanPipelineDO getCleanPipeline(Long id);

    /**
     * 获得清洗管道分页
     *
     * @param pageReqVO 分页查询
     * @return 清洗管道分页
     */
    PageResult<CleanPipelineDO> getCleanPipelinePage(CleanPipelinePageReqVO pageReqVO);

    /**
     * 获得所有启用的清洗管道列表
     *
     * @return 清洗管道列表
     */
    List<CleanPipelineDO> getCleanPipelineList();

}
