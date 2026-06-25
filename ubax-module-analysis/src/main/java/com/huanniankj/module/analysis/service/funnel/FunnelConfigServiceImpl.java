package com.huanniankj.module.analysis.service.funnel;

import cn.hutool.json.JSONUtil;
import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.analysis.controller.funnel.vo.FunnelConfigPageReqVO;
import com.huanniankj.module.analysis.controller.funnel.vo.FunnelConfigRespVO;
import com.huanniankj.module.analysis.controller.funnel.vo.FunnelConfigSaveReqVO;
import com.huanniankj.module.analysis.controller.funnel.vo.FunnelResultRespVO;
import com.huanniankj.module.analysis.convert.FunnelConfigConvert;
import com.huanniankj.module.analysis.dal.dataobject.funnel.FunnelConfigDO;
import com.huanniankj.module.analysis.dal.dataobject.funnel.FunnelResultDO;
import com.huanniankj.module.analysis.dal.mysql.funnel.FunnelConfigMapper;
import com.huanniankj.module.analysis.dal.mysql.funnel.FunnelResultMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static com.huanniankj.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.huanniankj.module.analysis.enums.ErrorCodeConstants.FUNNEL_CONFIG_NOT_EXISTS;

/**
 * @author zhaoff
 */
@Service
@Slf4j
public class FunnelConfigServiceImpl implements FunnelConfigService {

    @Resource
    private FunnelConfigMapper funnelConfigMapper;

    @Resource
    private FunnelResultMapper funnelResultMapper;

    @Override
    public Long createFunnelConfig(FunnelConfigSaveReqVO saveReqVO) {
        FunnelConfigDO config = FunnelConfigConvert.INSTANCE.convert(saveReqVO);
        funnelConfigMapper.insert(config);
        return config.getId();
    }

    @Override
    public void updateFunnelConfig(FunnelConfigSaveReqVO saveReqVO) {
        validateFunnelConfigExists(saveReqVO.getId());
        FunnelConfigDO updateObj = FunnelConfigConvert.INSTANCE.convert(saveReqVO);
        funnelConfigMapper.updateById(updateObj);
    }

    @Override
    public void deleteFunnelConfig(Long id) {
        validateFunnelConfigExists(id);
        funnelConfigMapper.deleteById(id);
    }

    @Override
    public FunnelConfigRespVO getFunnelConfig(Long id) {
        FunnelConfigDO config = funnelConfigMapper.selectById(id);
        return FunnelConfigConvert.INSTANCE.convert(config);
    }

    @Override
    public PageResult<FunnelConfigRespVO> getFunnelConfigPage(FunnelConfigPageReqVO pageReqVO) {
        PageResult<FunnelConfigDO> pageResult = funnelConfigMapper.selectPage(pageReqVO);
        return FunnelConfigConvert.INSTANCE.convertPage(pageResult);
    }

    private void validateFunnelConfigExists(Long id) {
        if (funnelConfigMapper.selectById(id) == null) {
            throw exception(FUNNEL_CONFIG_NOT_EXISTS);
        }
    }

    @Override
    public List<FunnelResultRespVO> getFunnelResults(Long configId, LocalDate startDate, LocalDate endDate) {
        List<FunnelResultDO> results = funnelResultMapper.selectByConfigIdAndDateRange(configId, startDate, endDate);
        return results.stream().map(this::convertResult).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private FunnelResultRespVO convertResult(FunnelResultDO resultDO) {
        List<FunnelResultRespVO.FunnelStepResult> steps = null;
        if (resultDO.getSteps() != null) {
            steps = JSONUtil.toList(resultDO.getSteps(), FunnelResultRespVO.FunnelStepResult.class);
        }
        return FunnelResultRespVO.builder()
                .id(resultDO.getId())
                .configId(resultDO.getConfigId())
                .statDate(resultDO.getStatDate())
                .totalUsers(resultDO.getTotalUsers())
                .finalConversionRate(resultDO.getFinalConversionRate())
                .avgConversionRate(resultDO.getAvgConversionRate())
                .avgDuration(resultDO.getAvgDuration())
                .steps(steps)
                .build();
    }

}
