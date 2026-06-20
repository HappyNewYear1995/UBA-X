package com.huanniankj.module.source.convert.event;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.source.controller.event.vo.EventRespVO;
import com.huanniankj.module.source.controller.event.vo.EventSaveReqVO;
import com.huanniankj.module.source.dal.dataobject.event.EventDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 事件管理 Convert
 *
 * @author zhaoff
 */
@Mapper
public interface EventConvert {

    EventConvert INSTANCE = Mappers.getMapper(EventConvert.class);

    EventDO convert(EventSaveReqVO bean);

    EventRespVO convert(EventDO bean);

    PageResult<EventRespVO> convertPage(PageResult<EventDO> bean);

}
