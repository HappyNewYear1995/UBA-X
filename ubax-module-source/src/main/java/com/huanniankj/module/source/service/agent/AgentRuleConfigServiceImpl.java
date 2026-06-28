package com.huanniankj.module.source.service.agent;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.source.controller.agent.vo.AgentRuleConfigPageReqVO;
import com.huanniankj.module.source.controller.agent.vo.AgentRuleConfigRespVO;
import com.huanniankj.module.source.controller.agent.vo.AgentRuleConfigSaveReqVO;
import com.huanniankj.module.source.convert.agent.EventConfigConvert;
import com.huanniankj.module.source.dal.dataobject.agent.AgentRuleConfigDO;
import com.huanniankj.module.source.dal.mysql.agent.AgentRuleConfigMapper;
import com.huanniankj.module.source.enums.agent.EventLevelEnum;
import com.huanniankj.module.source.enums.agent.MatchLogicEnum;
import com.huanniankj.module.source.enums.agent.MatchPositionEnum;
import com.huanniankj.module.source.enums.agent.MatchTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.huanniankj.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.huanniankj.module.source.enums.ErrorCodeConstants.EVENT_CONFIG_NOT_EXISTS;

/**
 * 事件配置服务实现
 *
 * @author zhaoff
 */
@Service("sourceEventConfigServiceImpl")
@Slf4j
public class AgentRuleConfigServiceImpl implements AgentRuleConfigService {

    @Resource
    private AgentRuleConfigMapper agentRuleConfigMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createEventConfig(AgentRuleConfigSaveReqVO saveReqVO) {
        AgentRuleConfigDO eventConfig = EventConfigConvert.INSTANCE.convert(saveReqVO);
        if (eventConfig.getEnabled() == null) {
            eventConfig.setEnabled(true);
        }
        if (eventConfig.getSort() == null) {
            eventConfig.setSort(0);
        }
        agentRuleConfigMapper.insert(eventConfig);
        log.info("事件配置已创建: id={}, name={}, position={}, type={}",
                eventConfig.getId(), eventConfig.getConfigName(), eventConfig.getMatchPosition(), eventConfig.getMatchType());
        return eventConfig.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEventConfig(AgentRuleConfigSaveReqVO saveReqVO) {
        if (saveReqVO.getId() == null) {
            throw exception(EVENT_CONFIG_NOT_EXISTS);
        }
        AgentRuleConfigDO existConfig = agentRuleConfigMapper.selectById(saveReqVO.getId());
        if (existConfig == null) {
            throw exception(EVENT_CONFIG_NOT_EXISTS);
        }
        AgentRuleConfigDO updateConfig = EventConfigConvert.INSTANCE.convert(saveReqVO);
        agentRuleConfigMapper.updateById(updateConfig);
        log.info("事件配置已更新: id={}, name={}", saveReqVO.getId(), saveReqVO.getConfigName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteEventConfig(Long id) {
        AgentRuleConfigDO existConfig = agentRuleConfigMapper.selectById(id);
        if (existConfig == null) {
            throw exception(EVENT_CONFIG_NOT_EXISTS);
        }
        agentRuleConfigMapper.deleteById(id);
        log.info("事件配置已删除: id={}", id);
    }

    @Override
    public AgentRuleConfigRespVO getEventConfig(Long id) {
        AgentRuleConfigDO eventConfig = agentRuleConfigMapper.selectById(id);
        if (eventConfig == null) {
            throw exception(EVENT_CONFIG_NOT_EXISTS);
        }
        return convertToRespVO(eventConfig);
    }

    @Override
    public PageResult<AgentRuleConfigRespVO> getEventConfigPage(AgentRuleConfigPageReqVO pageReqVO) {
        PageResult<AgentRuleConfigDO> pageResult = agentRuleConfigMapper.selectPage(pageReqVO);
        return EventConfigConvert.INSTANCE.convertPage(pageResult);
    }

    @Override
    public List<AgentRuleConfigRespVO> getEnabledEventConfigList() {
        List<AgentRuleConfigDO> configs = agentRuleConfigMapper.selectListByEnabled(true);
        return configs.stream()
                .map(this::convertToRespVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AgentRuleConfigRespVO> getEnabledEventConfigListByPosition(String matchPosition) {
        List<AgentRuleConfigDO> configs = agentRuleConfigMapper.selectListByMatchPosition(matchPosition);
        return configs.stream()
                .map(this::convertToRespVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AgentRuleConfigRespVO> matchEvent(String position, String content) {
        if (position == null || content == null) {
            return new ArrayList<>();
        }

        List<AgentRuleConfigDO> configs = agentRuleConfigMapper.selectListByMatchPosition(position);
        List<AgentRuleConfigDO> matchedConfigs = new ArrayList<>();

        for (AgentRuleConfigDO config : configs) {
            if (matchContent(content, config.getMatchType(), config.getMatchValue())) {
                matchedConfigs.add(config);
            }
        }

        log.info("事件匹配完成: position={}, content={}, matchedCount={}", position, content, matchedConfigs.size());
        return matchedConfigs.stream()
                .map(this::convertToRespVO)
                .collect(Collectors.toList());
    }

    /**
     * 匹配内容
     */
    private boolean matchContent(String content, String matchType, String matchValue) {
        if (content == null || matchValue == null) {
            return false;
        }

        MatchTypeEnum typeEnum = getMatchTypeEnum(matchType);
        if (typeEnum == null) {
            return false;
        }

        switch (typeEnum) {
            case CONTAINS:
                return content.contains(matchValue);
            case EQUALS:
                return content.equals(matchValue);
            case STARTS_WITH:
                return content.startsWith(matchValue);
            case ENDS_WITH:
                return content.endsWith(matchValue);
            case REGEX:
                try {
                    return Pattern.compile(matchValue).matcher(content).find();
                } catch (Exception e) {
                    log.error("正则表达式匹配失败: pattern={}, error={}", matchValue, e.getMessage());
                    return false;
                }
            case NOT_CONTAINS:
                return !content.contains(matchValue);
            default:
                return false;
        }
    }

    /**
     * 获取匹配类型枚举
     */
    private MatchTypeEnum getMatchTypeEnum(String type) {
        if (type == null) {
            return null;
        }
        for (MatchTypeEnum value : MatchTypeEnum.values()) {
            if (value.getType().equals(type)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 将 EventConfigDO 转换为 EventConfigRespVO，并填充枚举名称
     */
    private AgentRuleConfigRespVO convertToRespVO(AgentRuleConfigDO config) {
        AgentRuleConfigRespVO respVO = EventConfigConvert.INSTANCE.convert(config);
        respVO.setMatchPositionName(getMatchPositionName(config.getMatchPosition()));
        respVO.setMatchTypeName(getMatchTypeName(config.getMatchType()));
        respVO.setMatchLogicName(getMatchLogicName(config.getMatchLogic()));
        respVO.setEventLevelName(getEventLevelName(config.getEventLevel()));
        return respVO;
    }

    /**
     * 获取匹配位置名称
     */
    private String getMatchPositionName(String position) {
        if (position == null) {
            return null;
        }
        for (MatchPositionEnum value : MatchPositionEnum.values()) {
            if (value.getPosition().equals(position)) {
                return value.getName();
            }
        }
        return null;
    }

    /**
     * 获取匹配类型名称
     */
    private String getMatchTypeName(String type) {
        if (type == null) {
            return null;
        }
        for (MatchTypeEnum value : MatchTypeEnum.values()) {
            if (value.getType().equals(type)) {
                return value.getName();
            }
        }
        return null;
    }

    /**
     * 获取匹配逻辑名称
     */
    private String getMatchLogicName(String logic) {
        if (logic == null) {
            return null;
        }
        for (MatchLogicEnum value : MatchLogicEnum.values()) {
            if (value.getLogic().equals(logic)) {
                return value.getName();
            }
        }
        return null;
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

}
