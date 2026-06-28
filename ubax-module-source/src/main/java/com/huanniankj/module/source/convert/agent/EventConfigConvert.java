package com.huanniankj.module.source.convert.agent;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.source.controller.agent.vo.EventConfigRespVO;
import com.huanniankj.module.source.controller.agent.vo.EventConfigSaveReqVO;
import com.huanniankj.module.source.dal.dataobject.agent.AgentRuleConfigDO;
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

    AgentRuleConfigDO convert(EventConfigSaveReqVO bean);

    EventConfigRespVO convert(AgentRuleConfigDO bean);

    PageResult<EventConfigRespVO> convertPage(PageResult<AgentRuleConfigDO> bean);

}
