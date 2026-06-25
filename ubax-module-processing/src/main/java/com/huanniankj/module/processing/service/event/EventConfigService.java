package com.huanniankj.module.processing.service.event;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.processing.controller.event.vo.EventConfigPageReqVO;
import com.huanniankj.module.processing.dal.dataobject.event.EventConfigDO;

import java.util.List;

/**
 * 事件配置 Service 接口
 *
 * @author zhaoff
 */
public interface EventConfigService {

    /**
     * 创建事件配置
     *
     * @param eventConfig 事件配置
     * @return 事件配置 ID
     */
    Long createEventConfig(EventConfigDO eventConfig);

    /**
     * 更新事件配置
     *
     * @param eventConfig 事件配置
     */
    void updateEventConfig(EventConfigDO eventConfig);

    /**
     * 删除事件配置
     *
     * @param id 事件配置 ID
     */
    void deleteEventConfig(Long id);

    /**
     * 获得事件配置
     *
     * @param id 事件配置 ID
     * @return 事件配置
     */
    EventConfigDO getEventConfig(Long id);

    /**
     * 获得事件配置分页
     *
     * @param pageReqVO 分页查询
     * @return 事件配置分页
     */
    PageResult<EventConfigDO> getEventConfigPage(EventConfigPageReqVO pageReqVO);

    /**
     * 获得所有启用的事件配置列表
     *
     * @return 事件配置列表
     */
    List<EventConfigDO> getEventConfigList();

}
