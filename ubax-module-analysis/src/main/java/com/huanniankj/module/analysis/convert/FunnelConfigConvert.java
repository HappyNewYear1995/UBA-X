package com.huanniankj.module.analysis.convert;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.analysis.controller.vo.FunnelConfigRespVO;
import com.huanniankj.module.analysis.controller.vo.FunnelConfigSaveReqVO;
import com.huanniankj.module.analysis.dal.dataobject.FunnelConfigDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface FunnelConfigConvert {

    FunnelConfigConvert INSTANCE = Mappers.getMapper(FunnelConfigConvert.class);

    FunnelConfigRespVO convert(FunnelConfigDO bean);

    List<FunnelConfigRespVO> convertList(List<FunnelConfigDO> list);

    PageResult<FunnelConfigRespVO> convertPage(PageResult<FunnelConfigDO> page);

    FunnelConfigDO convert(FunnelConfigSaveReqVO bean);

}
