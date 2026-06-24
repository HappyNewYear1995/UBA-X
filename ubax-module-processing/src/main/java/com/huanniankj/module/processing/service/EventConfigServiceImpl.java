package com.huanniankj.module.processing.service;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.common.util.object.BeanUtils;
import com.huanniankj.module.processing.controller.vo.EventConfigPageReqVO;
import com.huanniankj.module.processing.controller.vo.EventConfigSaveReqVO;
import com.huanniankj.module.processing.dal.dataobject.EventConfigDO;
import com.huanniankj.module.processing.dal.mysql.EventConfigMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static com.huanniankj.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.huanniankj.module.processing.enums.ErrorCodeConstants.*;

/**
 * 事件配置服务实现类
 *
 * @author zhaoff
 */
@Service
@Validated
public class EventConfigServiceImpl implements EventConfigService {

    @Resource
    private EventConfigMapper eventConfigMapper;

    @Override
    public Long createEventConfig(EventConfigSaveReqVO createReqVO) {
        // 校验事件编码唯一
        validateCodeUnique(null, createReqVO.getCode());
        // 插入
        EventConfigDO eventConfig = BeanUtils.toBean(createReqVO, EventConfigDO.class);
        eventConfigMapper.insert(eventConfig);
        return eventConfig.getId();
    }

    @Override
    public void updateEventConfig(EventConfigSaveReqVO updateReqVO) {
        // 校验存在
        validateEventConfigExists(updateReqVO.getId());
        // 校验事件编码唯一
        validateCodeUnique(updateReqVO.getId(), updateReqVO.getCode());
        // 更新
        EventConfigDO updateObj = BeanUtils.toBean(updateReqVO, EventConfigDO.class);
        eventConfigMapper.updateById(updateObj);
    }

    @Override
    public void deleteEventConfig(Long id) {
        // 校验存在
        validateEventConfigExists(id);
        // 删除
        eventConfigMapper.deleteById(id);
    }

    @Override
    public EventConfigDO getEventConfig(Long id) {
        return eventConfigMapper.selectById(id);
    }

    @Override
    public PageResult<EventConfigDO> getEventConfigPage(EventConfigPageReqVO pageReqVO) {
        return eventConfigMapper.selectPage(pageReqVO);
    }

    @Override
    public List<EventConfigDO> getEventConfigList() {
        return eventConfigMapper.selectList(EventConfigDO::getStatus, 1);
    }

    private void validateEventConfigExists(Long id) {
        if (id == null) {
            return;
        }
        if (eventConfigMapper.selectById(id) == null) {
            throw exception(EVENT_CONFIG_NOT_EXISTS);
        }
    }

    private void validateCodeUnique(Long id, String code) {
        EventConfigDO existing = eventConfigMapper.selectByCode(code);
        if (existing == null) {
            return;
        }
        // 如果 id 为空，说明是新增，编码重复
        if (id == null) {
            throw exception(EVENT_CONFIG_CODE_DUPLICATE);
        }
        // 如果 id 不为空，说明是修改，编码重复且不是同一条记录
        if (!existing.getId().equals(id)) {
            throw exception(EVENT_CONFIG_CODE_DUPLICATE);
        }
    }

}
