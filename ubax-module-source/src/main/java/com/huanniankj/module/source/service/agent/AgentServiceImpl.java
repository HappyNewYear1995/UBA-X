package com.huanniankj.module.source.service.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.source.controller.agent.vo.*;
import com.huanniankj.module.source.controller.agent.vo.*;
import com.huanniankj.module.source.convert.agent.AgentConvert;
import com.huanniankj.module.source.dal.dataobject.agent.AgentDO;
import com.huanniankj.module.source.dal.mysql.agent.AgentMapper;
import com.huanniankj.module.source.enums.ErrorCodeConstants;
import com.huanniankj.module.source.enums.agent.AgentStatusEnum;
import com.huanniankj.module.source.enums.agent.CollectorStatusEnum;
import com.huanniankj.module.source.enums.agent.PlatformEnum;
import com.huanniankj.module.source.enums.agent.TerminalEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

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

    private final RestTemplate restTemplate = new RestTemplate();

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
            // 默认Vector配置
            agentInsert.setConfig("sources:\n  dummy_logs:\n    type: \"demo_logs\"\n    format: \"syslog\"\n    interval: 600\n\ntransforms:\n  parse_logs:\n    type: \"remap\"\n    inputs: [\"dummy_logs\"]\n    source: |\n      . = parse_syslog!(string!(.message))\n\nsinks:\n  print:\n    type: \"console\"\n    inputs: [\"parse_logs\"]\n    encoding:\n      codec: \"json\"\n      json:\n        pretty: true");
            // 当前时间为最后心跳时间
            agentInsert.setLastHeartbeat(LocalDateTime.now());
            agentMapper.insert(agentInsert);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receiveHeartbeat(AgentHeartbeatReqVO reqVO) {
        log.debug("收到心跳: {}", reqVO);

        // 查询数据库中是否已存在该 Agent
        AgentDO agent = agentMapper.selectByUUid(reqVO.getUuid());
        if (agent != null) {
            agent.setHostname(reqVO.getHostname());
            agent.setCollectorStatus(reqVO.getCollectorStatus());
            agent.setLastHeartbeat(LocalDateTime.now());
            agent.setOnline(true);
            agentMapper.updateById(agent);
        }
    }

    @Override
    public void pushConfig(String uuid) {
        // 校验 Agent 是否存在
        AgentDO agent = agentMapper.selectByUUid(uuid);
        if (agent == null) {
            throw exception(ErrorCodeConstants.AGENT_NOT_EXISTS);
        }
        // 校验 Agent 是否在线
        if (!Boolean.TRUE.equals(agent.getOnline())) {
            throw exception(ErrorCodeConstants.AGENT_OFFLINE);
        }

        try {
            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 构建请求体
            Map<String, Object> payload = Map.of(
                    "rules", agent.getConfig(),
                    "version", System.currentTimeMillis() / 1000
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            // 调用客户端 API 推送配置
            String agentApiUrl = "http://" + agent.getIp() + ":19090/api/config";
            ResponseEntity<String> response = restTemplate.postForEntity(agentApiUrl, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("配置已推送到 {}: {}", uuid, agentApiUrl);
            } else {
                log.error("推送配置失败，响应码: {}", response.getStatusCode());
                throw exception(ErrorCodeConstants.AGENT_PUSH_CONFIG_FAILED);
            }
        } catch (Exception e) {
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

        try {
            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 构建请求体
            Map<String, Object> payload = Map.of(
                    "action", reqVO.getAction(),
                    "params", reqVO.getParams() != null ? reqVO.getParams() : Map.of()
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            // 调用客户端 API 推送命令
            String agentApiUrl = "http://" + agent.getIp() + ":19090/api/command";
            ResponseEntity<String> response = restTemplate.postForEntity(agentApiUrl, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("命令已推送到 {}: action={}", uuid, reqVO.getAction());
            } else {
                log.error("推送命令失败，响应码: {}", response.getStatusCode());
                throw exception(ErrorCodeConstants.AGENT_PUSH_COMMAND_FAILED);
            }
        } catch (Exception e) {
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

    @Override
    public void updateAgentStatus(Long id, Integer status) {
        AgentDO agent = agentMapper.selectById(id);
        if (agent == null) {
            throw exception(ErrorCodeConstants.AGENT_NOT_EXISTS);
        }
        AgentDO update = new AgentDO();
        update.setId(id);
        update.setStatus(status);
        agentMapper.updateById(update);
    }

    @Override
    public void updateAgent(AgentUpdateReqVO updateReqVO) {
        AgentDO agent = agentMapper.selectById(updateReqVO.getId());
        if (agent == null) {
            throw exception(ErrorCodeConstants.AGENT_NOT_EXISTS);
        }
        AgentDO update = new AgentDO();
        update.setId(updateReqVO.getId());
        if (updateReqVO.getPlatform() != null) {
            update.setPlatform(updateReqVO.getPlatform());
        }
        if (updateReqVO.getConfig() != null) {
            update.setConfig(updateReqVO.getConfig());
        }
        if (updateReqVO.getRemark() != null) {
            update.setRemark(updateReqVO.getRemark());
        }
        agentMapper.updateById(update);
    }

}
