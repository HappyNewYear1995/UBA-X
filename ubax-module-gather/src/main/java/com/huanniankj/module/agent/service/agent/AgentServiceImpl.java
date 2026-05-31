package com.huanniankj.module.agent.service.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.agent.controller.agent.vo.*;
import com.huanniankj.module.agent.convert.agent.AgentConvert;
import com.huanniankj.module.agent.dal.dataobject.AgentDO;
import com.huanniankj.module.agent.dal.mysql.agent.AgentMapper;
import com.huanniankj.module.agent.enums.*;
import com.huanniankj.module.agent.enums.agent.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.huanniankj.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * Agent服务实现
 *
 * @author zhaoff
 */
@Service
@Slf4j
public class AgentServiceImpl implements AgentService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 存储在线客户端的 SSE 连接
     */
    private final Map<String, SseEmitter> clients = new ConcurrentHashMap<>();

    @Resource
    private AgentMapper agentMapper;

    @Override
    public void createAgent(AgentSaveReqVO saveReqVO) {
        AgentDO agentHistory = agentMapper.selectByUUid(saveReqVO.getUuid());
        if (agentHistory != null) {
            // 更新 Agent
            AgentDO agentUpdate = new AgentDO();
            agentUpdate.setId(agentHistory.getId());
            agentUpdate.setHostname(saveReqVO.getHostname());
            agentUpdate.setVersion(saveReqVO.getVersion());
            agentUpdate.setIp(saveReqVO.getIp());
            agentUpdate.setOs(saveReqVO.getOs());
            if (saveReqVO.getTerminal().equals(TerminalEnum.LINUX.getName())) {
                agentUpdate.setTerminal(TerminalEnum.LINUX.getTerminal());
            } else if (saveReqVO.getTerminal().equals(TerminalEnum.WINDOWS.getName())) {
                agentUpdate.setTerminal(TerminalEnum.WINDOWS.getTerminal());
            }
            // 默认在线
            agentUpdate.setOnline(true);
            // 当前时间为最后心跳时间
            agentUpdate.setLastHeartbeat(LocalDateTime.now());
            agentMapper.updateById(agentUpdate);
        } else {
            // 插入 Agent
            AgentDO agentInsert = new AgentDO();
            agentInsert.setUuid(saveReqVO.getUuid());
            agentInsert.setHostname(saveReqVO.getHostname());
            agentInsert.setVersion(saveReqVO.getVersion());
            agentInsert.setIp(saveReqVO.getIp());
            agentInsert.setOs(saveReqVO.getOs());
            if (saveReqVO.getTerminal().equals(TerminalEnum.LINUX.getName())) {
                agentInsert.setTerminal(TerminalEnum.LINUX.getTerminal());
            } else if (saveReqVO.getTerminal().equals(TerminalEnum.WINDOWS.getName())) {
                agentInsert.setTerminal(TerminalEnum.WINDOWS.getTerminal());
            }

            // Agent平台类型默认为自动
            agentInsert.setPlatform(PlatformEnum.AUTO.getPlatform());
            // Agent状态默认开启
            agentInsert.setStatus(AgentStatusEnum.ON.getStatus());
            // 采集器状态默认未知
            agentInsert.setCollectorStatus(CollectorStatusEnum.UNKNOWN.getStatus());
            // 默认在线
            agentInsert.setOnline(true);
            // 当前时间为最后心跳时间
            agentInsert.setLastHeartbeat(LocalDateTime.now());
            agentMapper.insert(agentInsert);
        }
    }

    @Override
    public void registerClient(String uuid, SseEmitter emitter) {
        clients.put(uuid, emitter);
        log.info("客户端已连接: {}", uuid);

        try {
            emitter.send(SseEmitter.event()
                    .name(EventTypeEnum.CONNECTED.getEventType())
                    .data("欢迎连接 UBAX-Pilot 推送服务"));
        } catch (IOException e) {
            log.error("发送欢迎消息失败: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receiveHeartbeat(AgentHeartbeatReqVO reqVO) {
        log.info("收到心跳: {}", reqVO);

        // 查询数据库中是否已存在该 Agent
        AgentDO agent = agentMapper.selectByUUid(reqVO.getUuid());
        if (agent != null) {
            agent.setHostname(reqVO.getHostname());
            agent.setCollectorStatus(reqVO.getCollectorStatus());
            agent.setLastHeartbeat(reqVO.getTimestamp() != null ? reqVO.getTimestamp() : LocalDateTime.now());
            agent.setOnline(true);
            agentMapper.updateById(agent);
        }
    }

    @Override
    public void removeClient(String uuid) {
        clients.remove(uuid);
        log.info("客户端已断开: {}", uuid);

        // 更新数据库中的在线状态
        AgentDO agent = agentMapper.selectByUUid(uuid);
        if (agent != null) {
            agent.setOnline(false);
            agentMapper.updateById(agent);
        }
    }

    @Override
    public void pushConfig(String uuid, String rules) {
        // 校验 Agent 是否存在
        AgentDO agent = agentMapper.selectByUUid(uuid);
        if (agent == null) {
            throw exception(ErrorCodeConstants.AGENT_NOT_EXISTS);
        }
        // 校验 Agent 是否在线
        if (!Boolean.TRUE.equals(agent.getOnline())) {
            throw exception(ErrorCodeConstants.AGENT_OFFLINE);
        }
        SseEmitter emitter = clients.get(uuid);
        if (emitter == null) {
            throw exception(ErrorCodeConstants.AGENT_OFFLINE);
        }

        try {
            Map<String, Object> payload = Map.of(
                    "rules", rules
            );

            Map<String, Object> message = Map.of(
                    "type", MessageTypeEnum.CONFIG.getType(),
                    "payload", payload,
                    "timestamp", System.currentTimeMillis() / 1000
            );

            emitter.send(SseEmitter.event()
                    .name(EventTypeEnum.MESSAGE.getEventType())
                    .data(objectMapper.writeValueAsString(message)));

            log.info("配置已推送到 {}: ", uuid);
        } catch (IOException e) {
            log.error("推送配置失败: {}", e.getMessage());
            throw exception(ErrorCodeConstants.AGENT_PUSH_CONFIG_FAILED);
        }
    }

    @Override
    public void pushCommand(AgentCommandReqVO reqVO) {
        String uuid = reqVO.getUuid();
        // 校验 Agent 是否存在
        AgentDO agent = agentMapper.selectByUUid(uuid);
        if (agent == null) {
            throw exception(ErrorCodeConstants.AGENT_NOT_EXISTS);
        }
        // 校验 Agent 是否在线
        if (!Boolean.TRUE.equals(agent.getOnline())) {
            throw exception(ErrorCodeConstants.AGENT_OFFLINE);
        }
        SseEmitter emitter = clients.get(uuid);
        if (emitter == null) {
            throw exception(ErrorCodeConstants.AGENT_OFFLINE);
        }

        try {
            Map<String, Object> payload = Map.of(
                    "action", reqVO.getAction(),
                    "params", Map.of()
            );

            Map<String, Object> message = Map.of(
                    "type", MessageTypeEnum.COMMAND.getType(),
                    "payload", payload,
                    "timestamp", System.currentTimeMillis() / 1000
            );

            emitter.send(SseEmitter.event()
                    .name(EventTypeEnum.CONNECTED.getEventType())
                    .data(objectMapper.writeValueAsString(message)));

            log.info("命令已推送到 {}: action={}", uuid, reqVO.getAction());
        } catch (IOException e) {
            log.error("推送命令失败: {}", e.getMessage());
            throw exception(ErrorCodeConstants.AGENT_PUSH_COMMAND_FAILED);
        }
    }

    @Override
    public PageResult<AgentRespVO> getAgentPage(AgentPageReqVO pageReqVO) {
        PageResult<AgentDO> pageResult = agentMapper.selectPage(pageReqVO);
        return AgentConvert.INSTANCE.convertPage(pageResult);
    }

    @Override
    public AgentRespVO getAgent(Long id) {
        AgentDO agent = agentMapper.selectById(id);
        if (agent == null) {
            throw exception(ErrorCodeConstants.AGENT_NOT_EXISTS);
        }
        return AgentConvert.INSTANCE.convert(agent);
    }

}
