package com.huanniankj.module.processing.service.event;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.common.util.object.BeanUtils;
import com.huanniankj.module.processing.controller.event.vo.EventPageReqVO;
import com.huanniankj.module.processing.controller.event.vo.EventSaveReqVO;
import com.huanniankj.module.processing.dal.dataobject.event.EventDO;
import com.huanniankj.module.processing.dal.mysql.event.EventMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static com.huanniankj.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.huanniankj.module.processing.enums.ErrorCodeConstants.*;

/**
 * 事件 Service 实现类
 * <p>
 * 事件为不可变对象（Immutable），不提供更新方法，仅支持创建、删除和查询。
 *
 * @author zhaoff
 */
@Service
@Validated
public class EventServiceImpl implements EventService {

    @Resource
    private EventMapper eventMapper;

    @Override
    public Long createEvent(EventSaveReqVO createReqVO) {
        // 插入
        EventDO event = BeanUtils.toBean(createReqVO, EventDO.class);
        eventMapper.insert(event);
        return event.getId();
    }

    @Override
    public void deleteEvent(Long id) {
        // 校验存在
        validateEventExists(id);
        // 删除
        eventMapper.deleteById(id);
    }

    @Override
    public EventDO getEvent(Long id) {
        return eventMapper.selectById(id);
    }

    @Override
    public PageResult<EventDO> getEventPage(EventPageReqVO pageReqVO) {
        return eventMapper.selectPage(pageReqVO);
    }

    @Override
    public List<EventDO> getEventListByCategory(String eventCategory) {
        return eventMapper.selectList(EventDO::getEventCategory, eventCategory);
    }

    @Override
    public List<EventDO> getEventListByActorId(String actorId) {
        return eventMapper.selectList(EventDO::getActorId, actorId);
    }

    private void validateEventExists(Long id) {
        if (id == null) {
            return;
        }
        if (eventMapper.selectById(id) == null) {
            throw exception(EVENT_NOT_EXISTS);
        }
    }

}
