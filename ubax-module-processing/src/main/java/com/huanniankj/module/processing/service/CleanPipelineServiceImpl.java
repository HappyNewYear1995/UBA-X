package com.huanniankj.module.processing.service;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.common.util.object.BeanUtils;
import com.huanniankj.module.processing.controller.vo.CleanPipelinePageReqVO;
import com.huanniankj.module.processing.controller.vo.CleanPipelineSaveReqVO;
import com.huanniankj.module.processing.dal.dataobject.CleanPipelineDO;
import com.huanniankj.module.processing.dal.mysql.CleanPipelineMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static com.huanniankj.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.huanniankj.module.processing.enums.ErrorCodeConstants.*;

/**
 * 清洗管道服务实现类
 *
 * @author zhaoff
 */
@Service
@Validated
public class CleanPipelineServiceImpl implements CleanPipelineService {

    @Resource
    private CleanPipelineMapper cleanPipelineMapper;

    @Override
    public Long createCleanPipeline(CleanPipelineSaveReqVO createReqVO) {
        // 校验管道名称唯一
        validateNameUnique(null, createReqVO.getName());
        // 插入
        CleanPipelineDO cleanPipeline = BeanUtils.toBean(createReqVO, CleanPipelineDO.class);
        cleanPipelineMapper.insert(cleanPipeline);
        return cleanPipeline.getId();
    }

    @Override
    public void updateCleanPipeline(CleanPipelineSaveReqVO updateReqVO) {
        // 校验存在
        validateCleanPipelineExists(updateReqVO.getId());
        // 校验管道名称唯一
        validateNameUnique(updateReqVO.getId(), updateReqVO.getName());
        // 更新
        CleanPipelineDO updateObj = BeanUtils.toBean(updateReqVO, CleanPipelineDO.class);
        cleanPipelineMapper.updateById(updateObj);
    }

    @Override
    public void deleteCleanPipeline(Long id) {
        // 校验存在
        validateCleanPipelineExists(id);
        // 删除
        cleanPipelineMapper.deleteById(id);
    }

    @Override
    public CleanPipelineDO getCleanPipeline(Long id) {
        return cleanPipelineMapper.selectById(id);
    }

    @Override
    public PageResult<CleanPipelineDO> getCleanPipelinePage(CleanPipelinePageReqVO pageReqVO) {
        return cleanPipelineMapper.selectPage(pageReqVO);
    }

    @Override
    public List<CleanPipelineDO> getCleanPipelineList() {
        return cleanPipelineMapper.selectList(CleanPipelineDO::getStatus, 1);
    }

    private void validateCleanPipelineExists(Long id) {
        if (id == null) {
            return;
        }
        if (cleanPipelineMapper.selectById(id) == null) {
            throw exception(CLEAN_PIPELINE_NOT_EXISTS);
        }
    }

    private void validateNameUnique(Long id, String name) {
        CleanPipelineDO existing = cleanPipelineMapper.selectByName(name);
        if (existing == null) {
            return;
        }
        // 如果 id 为空，说明是新增，名称重复
        if (id == null) {
            throw exception(CLEAN_PIPELINE_NAME_DUPLICATE);
        }
        // 如果 id 不为空，说明是修改，名称重复且不是同一条记录
        if (!existing.getId().equals(id)) {
            throw exception(CLEAN_PIPELINE_NAME_DUPLICATE);
        }
    }

}
