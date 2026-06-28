package com.huanniankj.module.source.service.agent;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.source.controller.agent.vo.EventHandleReqVO;
import com.huanniankj.module.source.controller.agent.vo.EventPageReqVO;
import com.huanniankj.module.source.controller.agent.vo.EventRespVO;
import com.huanniankj.module.source.controller.agent.vo.EventSaveReqVO;
import com.huanniankj.module.source.convert.agent.EventConvert;
import com.huanniankj.module.source.dal.dataobject.agent.AgentLogDO;
import com.huanniankj.module.source.dal.mysql.event.AgentEventMapper;
import com.huanniankj.module.source.enums.event.EventLevelEnum;
import com.huanniankj.module.source.enums.event.EventSourceEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.huanniankj.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.huanniankj.module.source.enums.ErrorCodeConstants.EVENT_NOT_EXISTS;

/**
 * 事件管理服务实现
 *
 * @author zhaoff
 */
@Service("sourceEventServiceImpl")
@Slf4j
public class EventServiceImpl implements EventService {

    @Resource
    private AgentEventMapper agentEventMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createEvent(EventSaveReqVO saveReqVO) {
        AgentLogDO event = EventConvert.INSTANCE.convert(saveReqVO);
        if (event.getEventTime() == null) {
            event.setEventTime(LocalDateTime.now());
        }
        event.setHandled(false);
        agentEventMapper.insert(event);
        log.info("事件已创建: id={}, type={}, level={}", event.getId(), event.getEventType(), event.getEventLevel());
        return event.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleEvent(EventHandleReqVO handleReqVO) {
        AgentLogDO event = agentEventMapper.selectById(handleReqVO.getId());
        if (event == null) {
            throw exception(EVENT_NOT_EXISTS);
        }
        event.setHandled(true);
        event.setHandler(handleReqVO.getHandler());
        event.setHandleTime(LocalDateTime.now());
        event.setHandleRemark(handleReqVO.getHandleRemark());
        agentEventMapper.updateById(event);
        log.info("事件已处理: id={}, handler={}", event.getId(), handleReqVO.getHandler());
    }

    @Override
    public EventRespVO getEvent(Long id) {
        AgentLogDO event = agentEventMapper.selectById(id);
        if (event == null) {
            throw exception(EVENT_NOT_EXISTS);
        }
        return convertToRespVO(event);
    }

    @Override
    public PageResult<EventRespVO> getEventPage(EventPageReqVO pageReqVO) {
        PageResult<AgentLogDO> pageResult = agentEventMapper.selectPage(pageReqVO);
        return EventConvert.INSTANCE.convertPage(pageResult);
    }

    @Override
    public List<EventRespVO> getEventListByAgentUuid(String agentUuid) {
        List<AgentLogDO> events = agentEventMapper.selectListByAgentUuid(agentUuid);
        return events.stream()
                .map(this::convertToRespVO)
                .collect(Collectors.toList());
    }

    @Override
    public Long countEventByAgentUuidAndLevel(String agentUuid, Integer eventLevel) {
        return agentEventMapper.selectCountByAgentUuidAndLevel(agentUuid, eventLevel);
    }

    @Override
    public Long countEventByAgentUuidAndHandled(String agentUuid, Boolean handled) {
        return agentEventMapper.selectCountByAgentUuidAndHandled(agentUuid, handled);
    }

    /**
     * 将 EventDO 转换为 EventRespVO，并填充枚举名称
     */
    private EventRespVO convertToRespVO(AgentLogDO event) {
        EventRespVO respVO = EventConvert.INSTANCE.convert(event);
        respVO.setEventLevelName(getEventLevelName(event.getEventLevel()));
        respVO.setEventSourceName(getEventSourceName(event.getEventSource()));
        return respVO;
    }

    /**
     * 获取事件级别名称
     */
    private String getEventLevelName(Integer level) {
        if (level == null) {
            return null;
        }
        for (EventLevelEnum value : EventLevelEnum.values()) {
            if (value.getLevel().equals(level)) {
                return value.getName();
            }
        }
        return null;
    }

    /**
     * 获取事件来源名称
     */
    private String getEventSourceName(String source) {
        if (source == null) {
            return null;
        }
        for (EventSourceEnum value : EventSourceEnum.values()) {
            if (value.getSource().equals(source)) {
                return value.getName();
            }
        }
        return null;
    }

}
