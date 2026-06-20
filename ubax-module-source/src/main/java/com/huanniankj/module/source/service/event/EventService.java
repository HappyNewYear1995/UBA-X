package com.huanniankj.module.source.service.event;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.source.controller.event.vo.EventHandleReqVO;
import com.huanniankj.module.source.controller.event.vo.EventPageReqVO;
import com.huanniankj.module.source.controller.event.vo.EventRespVO;
import com.huanniankj.module.source.controller.event.vo.EventSaveReqVO;

/**
 * 事件管理服务接口
 *
 * @author zhaoff
 */
public interface EventService {

    /**
     * 创建事件
     *
     * @param saveReqVO 创建事件请求
     * @return 事件 ID
     */
    Long createEvent(EventSaveReqVO saveReqVO);

    /**
     * 处理事件
     *
     * @param handleReqVO 处理事件请求
     */
    void handleEvent(EventHandleReqVO handleReqVO);

    /**
     * 获取事件详情
     *
     * @param id 事件 ID
     * @return 事件详情
     */
    EventRespVO getEvent(Long id);

    /**
     * 获取事件分页列表
     *
     * @param pageReqVO 分页查询
     * @return 事件分页结果
     */
    PageResult<EventRespVO> getEventPage(EventPageReqVO pageReqVO);

    /**
     * 根据 Agent UUID 获取事件列表
     *
     * @param agentUuid Agent UUID
     * @return 事件列表
     */
    java.util.List<EventRespVO> getEventListByAgentUuid(String agentUuid);

    /**
     * 根据 Agent UUID 和事件级别统计事件数量
     *
     * @param agentUuid  Agent UUID
     * @param eventLevel 事件级别
     * @return 事件数量
     */
    Long countEventByAgentUuidAndLevel(String agentUuid, Integer eventLevel);

    /**
     * 根据 Agent UUID 和处理状态统计事件数量
     *
     * @param agentUuid Agent UUID
     * @param handled   是否已处理
     * @return 事件数量
     */
    Long countEventByAgentUuidAndHandled(String agentUuid, Boolean handled);

}
