package com.huanniankj.module.gather.convert.monitor;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.gather.controller.monitor.vo.MonitorMetricRespVO;
import com.huanniankj.module.gather.controller.monitor.vo.MonitorMetricSaveReqVO;
import com.huanniankj.module.gather.dal.dataobject.monitor.MonitorMetricDO;
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

    MonitorMetricDO convert(MonitorMetricSaveReqVO bean);

    MonitorMetricRespVO convert(MonitorMetricDO bean);

    PageResult<MonitorMetricRespVO> convertPage(PageResult<MonitorMetricDO> bean);

}
