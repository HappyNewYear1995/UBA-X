package com.huanniankj.module.analysis.service.retention;

import cn.hutool.json.JSONUtil;
import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.analysis.controller.retention.vo.RetentionConfigPageReqVO;
import com.huanniankj.module.analysis.controller.retention.vo.RetentionConfigRespVO;
import com.huanniankj.module.analysis.controller.retention.vo.RetentionConfigSaveReqVO;
import com.huanniankj.module.analysis.controller.retention.vo.RetentionResultRespVO;
import com.huanniankj.module.analysis.convert.RetentionConfigConvert;
import com.huanniankj.module.analysis.dal.dataobject.retention.RetentionConfigDO;
import com.huanniankj.module.analysis.dal.dataobject.retention.RetentionResultDO;
import com.huanniankj.module.analysis.dal.mysql.retention.RetentionConfigMapper;
import com.huanniankj.module.analysis.dal.mysql.retention.RetentionResultMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.huanniankj.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.huanniankj.module.analysis.enums.ErrorCodeConstants.RETENTION_CONFIG_NOT_EXISTS;

/**
 * @author zhaoff
 */
@Service
@Slf4j
public class RetentionConfigServiceImpl implements RetentionConfigService {

    @Resource
    private RetentionConfigMapper retentionConfigMapper;

    @Resource
    private RetentionResultMapper retentionResultMapper;

    @Override
    public Long createRetentionConfig(RetentionConfigSaveReqVO saveReqVO) {
        RetentionConfigDO config = RetentionConfigConvert.INSTANCE.convert(saveReqVO);
        retentionConfigMapper.insert(config);
        return config.getId();
    }

    @Override
    public void updateRetentionConfig(RetentionConfigSaveReqVO saveReqVO) {
        validateRetentionConfigExists(saveReqVO.getId());
        RetentionConfigDO updateObj = RetentionConfigConvert.INSTANCE.convert(saveReqVO);
        retentionConfigMapper.updateById(updateObj);
    }

    @Override
    public void deleteRetentionConfig(Long id) {
        validateRetentionConfigExists(id);
        retentionConfigMapper.deleteById(id);
    }

    @Override
    public RetentionConfigRespVO getRetentionConfig(Long id) {
        RetentionConfigDO config = retentionConfigMapper.selectById(id);
        return RetentionConfigConvert.INSTANCE.convert(config);
    }

    @Override
    public PageResult<RetentionConfigRespVO> getRetentionConfigPage(RetentionConfigPageReqVO pageReqVO) {
        PageResult<RetentionConfigDO> pageResult = retentionConfigMapper.selectPage(pageReqVO);
        return RetentionConfigConvert.INSTANCE.convertPage(pageResult);
    }

    @Override
    public List<RetentionResultRespVO> getRetentionResults(Long configId, LocalDate startDate, LocalDate endDate) {
        List<RetentionResultDO> results = retentionResultMapper.selectByConfigIdAndDateRange(configId, startDate, endDate);
        return results.stream().map(this::convertResult).collect(Collectors.toList());
    }

    private void validateRetentionConfigExists(Long id) {
        if (retentionConfigMapper.selectById(id) == null) {
            throw exception(RETENTION_CONFIG_NOT_EXISTS);
        }
    }

    @SuppressWarnings("unchecked")
    private RetentionResultRespVO convertResult(RetentionResultDO resultDO) {
        Map<Integer, Long> retentionUsers = null;
        if (resultDO.getRetentionUsers() != null) {
            retentionUsers = JSONUtil.toBean(resultDO.getRetentionUsers(), Map.class);
        }
        Map<Integer, Double> retentionRates = null;
        if (resultDO.getRetentionRates() != null) {
            retentionRates = JSONUtil.toBean(resultDO.getRetentionRates(), Map.class);
        }
        return RetentionResultRespVO.builder()
                .id(resultDO.getId())
                .configId(resultDO.getConfigId())
                .statDate(resultDO.getStatDate())
                .newUsers(resultDO.getNewUsers())
                .retentionUsers(retentionUsers)
                .retentionRates(retentionRates)
                .build();
    }

}
