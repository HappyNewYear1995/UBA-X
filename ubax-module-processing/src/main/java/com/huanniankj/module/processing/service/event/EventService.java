package com.huanniankj.module.processing.service.event;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.processing.controller.event.vo.EventPageReqVO;
import com.huanniankj.module.processing.controller.event.vo.EventSaveReqVO;
import com.huanniankj.module.processing.dal.dataobject.event.EventDO;

import java.util.List;

/**
 * 事件 Service 接口
 * <p>
 * 事件为不可变对象（Immutable），不提供更新方法，仅支持创建、删除和查询。
 *
 * @author zhaoff
 */
public interface EventService {

    /**
     * 创建事件
     *
     * @param createReqVO 创建信息
     * @return 事件 ID
     */
    Long createEvent(EventSaveReqVO createReqVO);

    /**
     * 删除事件
     *
     * @param id 事件 ID
     */
    void deleteEvent(Long id);

    /**
     * 获得事件
     *
     * @param id 事件 ID
     * @return 事件
     */
    EventDO getEvent(Long id);

    /**
     * 获得事件分页
     *
     * @param pageReqVO 分页查询
     * @return 事件分页
     */
    PageResult<EventDO> getEventPage(EventPageReqVO pageReqVO);

    /**
     * 根据事件大类获取事件列表
     *
     * @param eventCategory 事件大类
     * @return 事件列表
     */
    List<EventDO> getEventListByCategory(String eventCategory);

    /**
     * 根据行为主体标识获取事件列表
     *
     * @param actorId 行为主体唯一标识
     * @return 事件列表
     */
    List<EventDO> getEventListByActorId(String actorId);

}
