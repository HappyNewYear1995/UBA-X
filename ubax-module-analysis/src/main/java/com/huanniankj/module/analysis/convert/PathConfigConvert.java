package com.huanniankj.module.analysis.convert;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.analysis.controller.admin.vo.PathConfigRespVO;
import com.huanniankj.module.analysis.controller.admin.vo.PathConfigSaveReqVO;
import com.huanniankj.module.analysis.dal.dataobject.PathConfigDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 路径分析配置 Convert
 *
 * @author zhaoff
 */
@Mapper
public interface PathConfigConvert {

    PathConfigConvert INSTANCE = Mappers.getMapper(PathConfigConvert.class);

    PathConfigRespVO convert(PathConfigDO bean);

    List<PathConfigRespVO> convertList(List<PathConfigDO> list);

    PageResult<PathConfigRespVO> convertPage(PageResult<PathConfigDO> page);

    PathConfigDO convert(PathConfigSaveReqVO bean);

}
