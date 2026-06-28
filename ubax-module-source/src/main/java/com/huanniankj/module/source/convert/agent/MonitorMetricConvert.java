package com.huanniankj.module.source.convert.agent;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.source.controller.agent.vo.MonitorMetricRespVO;
import com.huanniankj.module.source.controller.agent.vo.MonitorMetricSaveReqVO;
import com.huanniankj.module.source.dal.dataobject.agent.AgentMonitorMetricDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 运行监控 Convert
 *
 * @author zhaoff
 */
@Mapper
public interface MonitorMetricConvert {

    MonitorMetricConvert INSTANCE = Mappers.getMapper(MonitorMetricConvert.class);

    AgentMonitorMetricDO convert(MonitorMetricSaveReqVO bean);

    MonitorMetricRespVO convert(AgentMonitorMetricDO bean);

    PageResult<MonitorMetricRespVO> convertPage(PageResult<AgentMonitorMetricDO> bean);

}
