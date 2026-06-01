package com.huanniankj.module.gather.convert.agent;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.gather.controller.admin.agent.vo.AgentRespVO;
import com.huanniankj.module.gather.dal.dataobject.agent.AgentDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Agent Convert
 *
 * @author zhaoff
 */
@Mapper
public interface AgentConvert {

    AgentConvert INSTANCE = Mappers.getMapper(AgentConvert.class);

    AgentRespVO convert(AgentDO bean);

    PageResult<AgentRespVO> convertPage(PageResult<AgentDO> page);

}
