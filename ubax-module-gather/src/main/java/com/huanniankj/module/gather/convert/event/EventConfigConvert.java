package com.huanniankj.module.gather.convert.event;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.gather.controller.event.vo.EventConfigRespVO;
import com.huanniankj.module.gather.controller.event.vo.EventConfigSaveReqVO;
import com.huanniankj.module.gather.dal.dataobject.event.EventConfigDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 事件配置 Convert
 *
 * @author zhaoff
 */
@Mapper
public interface EventConfigConvert {

    EventConfigConvert INSTANCE = Mappers.getMapper(EventConfigConvert.class);

    EventConfigDO convert(EventConfigSaveReqVO bean);

    EventConfigRespVO convert(EventConfigDO bean);

    PageResult<EventConfigRespVO> convertPage(PageResult<EventConfigDO> bean);

}
