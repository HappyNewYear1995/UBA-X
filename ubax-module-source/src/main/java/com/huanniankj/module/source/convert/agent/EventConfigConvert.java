package com.huanniankj.module.source.convert.agent;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.source.controller.agent.vo.AgentRuleConfigRespVO;
import com.huanniankj.module.source.controller.agent.vo.AgentRuleConfigSaveReqVO;
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

    AgentRuleConfigDO convert(AgentRuleConfigSaveReqVO bean);

    AgentRuleConfigRespVO convert(AgentRuleConfigDO bean);

    PageResult<AgentRuleConfigRespVO> convertPage(PageResult<AgentRuleConfigDO> bean);

}
