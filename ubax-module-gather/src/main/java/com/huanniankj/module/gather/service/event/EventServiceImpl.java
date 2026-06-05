package com.huanniankj.module.gather.service.event;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.gather.controller.event.vo.EventHandleReqVO;
import com.huanniankj.module.gather.controller.event.vo.EventPageReqVO;
import com.huanniankj.module.gather.controller.event.vo.EventRespVO;
import com.huanniankj.module.gather.controller.event.vo.EventSaveReqVO;
import com.huanniankj.module.gather.convert.event.EventConvert;
import com.huanniankj.module.gather.dal.dataobject.event.EventDO;
import com.huanniankj.module.gather.dal.mysql.event.EventMapper;
import com.huanniankj.module.gather.enums.event.EventLevelEnum;
import com.huanniankj.module.gather.enums.event.EventSourceEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.huanniankj.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.huanniankj.module.gather.enums.ErrorCodeConstants.EVENT_NOT_EXISTS;

/**
 * 事件管理服务实现
 *
 * @author zhaoff
 */
@Service
@Slf4j
public class EventServiceImpl implements EventService {

    @Resource
    private EventMapper eventMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createEvent(EventSaveReqVO saveReqVO) {
        EventDO event = EventConvert.INSTANCE.convert(saveReqVO);
        if (event.getEventTime() == null) {
            event.setEventTime(LocalDateTime.now());
        }
        event.setHandled(false);
        eventMapper.insert(event);
        log.info("事件已创建: id={}, type={}, level={}", event.getId(), event.getEventType(), event.getEventLevel());
        return event.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleEvent(EventHandleReqVO handleReqVO) {
        EventDO event = eventMapper.selectById(handleReqVO.getId());
        if (event == null) {
            throw exception(EVENT_NOT_EXISTS);
        }
        event.setHandled(true);
        event.setHandler(handleReqVO.getHandler());
        event.setHandleTime(LocalDateTime.now());
        event.setHandleRemark(handleReqVO.getHandleRemark());
        eventMapper.updateById(event);
        log.info("事件已处理: id={}, handler={}", event.getId(), handleReqVO.getHandler());
    }

    @Override
    public EventRespVO getEvent(Long id) {
        EventDO event = eventMapper.selectById(id);
        if (event == null) {
            throw exception(EVENT_NOT_EXISTS);
        }
        return convertToRespVO(event);
    }

    @Override
    public PageResult<EventRespVO> getEventPage(EventPageReqVO pageReqVO) {
        PageResult<EventDO> pageResult = eventMapper.selectPage(pageReqVO);
        return EventConvert.INSTANCE.convertPage(pageResult);
    }

    @Override
    public List<EventRespVO> getEventListByAgentUuid(String agentUuid) {
        List<EventDO> events = eventMapper.selectListByAgentUuid(agentUuid);
        return events.stream()
                .map(this::convertToRespVO)
                .collect(Collectors.toList());
    }

    @Override
    public Long countEventByAgentUuidAndLevel(String agentUuid, Integer eventLevel) {
        return eventMapper.selectCountByAgentUuidAndLevel(agentUuid, eventLevel);
    }

    @Override
    public Long countEventByAgentUuidAndHandled(String agentUuid, Boolean handled) {
        return eventMapper.selectCountByAgentUuidAndHandled(agentUuid, handled);
    }

    /**
     * 将 EventDO 转换为 EventRespVO，并填充枚举名称
     */
    private EventRespVO convertToRespVO(EventDO event) {
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
