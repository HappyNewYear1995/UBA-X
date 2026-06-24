package com.huanniankj.module.analysis.service;

import cn.hutool.json.JSONUtil;
import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.analysis.controller.vo.PathConfigPageReqVO;
import com.huanniankj.module.analysis.controller.vo.PathConfigRespVO;
import com.huanniankj.module.analysis.controller.vo.PathConfigSaveReqVO;
import com.huanniankj.module.analysis.controller.vo.PathResultRespVO;
import com.huanniankj.module.analysis.convert.PathConfigConvert;
import com.huanniankj.module.analysis.dal.dataobject.PathConfigDO;
import com.huanniankj.module.analysis.dal.dataobject.PathResultDO;
import com.huanniankj.module.analysis.dal.mysql.PathConfigMapper;
import com.huanniankj.module.analysis.dal.mysql.PathResultMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static com.huanniankj.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.huanniankj.module.analysis.enums.ErrorCodeConstants.PATH_CONFIG_NOT_EXISTS;

/**
 * 路径分析配置服务实现
 *
 * @author zhaoff
 */
@Service
@Slf4j
public class PathConfigServiceImpl implements PathConfigService {

    @Resource
    private PathConfigMapper pathConfigMapper;

    @Resource
    private PathResultMapper pathResultMapper;

    @Override
    public Long createPathConfig(PathConfigSaveReqVO saveReqVO) {
        PathConfigDO config = PathConfigConvert.INSTANCE.convert(saveReqVO);
        pathConfigMapper.insert(config);
        return config.getId();
    }

    @Override
    public void updatePathConfig(PathConfigSaveReqVO saveReqVO) {
        validatePathConfigExists(saveReqVO.getId());
        PathConfigDO updateObj = PathConfigConvert.INSTANCE.convert(saveReqVO);
        pathConfigMapper.updateById(updateObj);
    }

    @Override
    public void deletePathConfig(Long id) {
        validatePathConfigExists(id);
        pathConfigMapper.deleteById(id);
    }

    @Override
    public PathConfigRespVO getPathConfig(Long id) {
        PathConfigDO config = pathConfigMapper.selectById(id);
        return PathConfigConvert.INSTANCE.convert(config);
    }

    @Override
    public PageResult<PathConfigRespVO> getPathConfigPage(PathConfigPageReqVO pageReqVO) {
        PageResult<PathConfigDO> pageResult = pathConfigMapper.selectPage(pageReqVO);
        return PathConfigConvert.INSTANCE.convertPage(pageResult);
    }

    @Override
    public List<PathResultRespVO> getPathResults(Long configId, LocalDate startDate, LocalDate endDate) {
        List<PathResultDO> results = pathResultMapper.selectByConfigIdAndDateRange(configId, startDate, endDate);
        return results.stream().map(this::convertResult).collect(Collectors.toList());
    }

    private void validatePathConfigExists(Long id) {
        if (pathConfigMapper.selectById(id) == null) {
            throw exception(PATH_CONFIG_NOT_EXISTS);
        }
    }

    @SuppressWarnings("unchecked")
    private PathResultRespVO convertResult(PathResultDO resultDO) {
        List<PathResultRespVO.SankeyNode> nodes = null;
        if (resultDO.getNodes() != null) {
            nodes = JSONUtil.toList(resultDO.getNodes(), PathResultRespVO.SankeyNode.class);
        }
        List<PathResultRespVO.SankeyLink> links = null;
        if (resultDO.getLinks() != null) {
            links = JSONUtil.toList(resultDO.getLinks(), PathResultRespVO.SankeyLink.class);
        }
        List<PathResultRespVO.PathStat> pathStats = null;
        if (resultDO.getPathStats() != null) {
            pathStats = JSONUtil.toList(resultDO.getPathStats(), PathResultRespVO.PathStat.class);
        }
        return PathResultRespVO.builder()
                .id(resultDO.getId())
                .configId(resultDO.getConfigId())
                .statDate(resultDO.getStatDate())
                .nodes(nodes)
                .links(links)
                .pathStats(pathStats)
                .build();
    }

}
