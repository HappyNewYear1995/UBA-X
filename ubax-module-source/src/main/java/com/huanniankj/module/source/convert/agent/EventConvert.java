package com.huanniankj.module.source.convert.agent;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.source.controller.agent.vo.AgentLogRespVO;
import com.huanniankj.module.source.controller.agent.vo.AgentLogSaveReqVO;
import com.huanniankj.module.source.dal.dataobject.agent.AgentLogDO;
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

    AgentLogDO convert(AgentLogSaveReqVO bean);

    AgentLogRespVO convert(AgentLogDO bean);

    PageResult<AgentLogRespVO> convertPage(PageResult<AgentLogDO> bean);

}
