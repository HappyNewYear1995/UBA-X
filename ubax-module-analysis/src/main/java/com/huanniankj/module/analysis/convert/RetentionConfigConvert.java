package com.huanniankj.module.analysis.convert;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.analysis.controller.retention.vo.RetentionConfigRespVO;
import com.huanniankj.module.analysis.controller.retention.vo.RetentionConfigSaveReqVO;
import com.huanniankj.module.analysis.dal.dataobject.retention.RetentionConfigDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author zhaoff
 */
@Mapper
public interface RetentionConfigConvert {

    RetentionConfigConvert INSTANCE = Mappers.getMapper(RetentionConfigConvert.class);

    RetentionConfigRespVO convert(RetentionConfigDO bean);

    List<RetentionConfigRespVO> convertList(List<RetentionConfigDO> list);

    PageResult<RetentionConfigRespVO> convertPage(PageResult<RetentionConfigDO> page);

    RetentionConfigDO convert(RetentionConfigSaveReqVO bean);

}
