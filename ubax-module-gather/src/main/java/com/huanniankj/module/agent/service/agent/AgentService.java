package com.huanniankj.module.agent.service.agent;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.agent.controller.agent.vo.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Agent服务接口
 *
 * @author zhaoff
 */
public interface AgentService {

    /**
     * 创建 Agent
     *
     * @param saveReqVO 创建 Agent 请求
     */
    void createAgent(AgentSaveReqVO saveReqVO);

    /**
     * 注册 SSE 推送客户端
     *
     * @param uuid    agent uuid
     * @param emitter SSE 推送器
     */
    void registerClient(String uuid, SseEmitter emitter);

    /**
     * 接收心跳上报
     *
     * @param reqVO 心跳数据
     */
    void receiveHeartbeat(AgentHeartbeatReqVO reqVO);

    /**
     * 移除客户端
     *
     * @param uuid Agent UUID
     */
    void removeClient(String uuid);

    /**
     * 推送配置到指定客户端
     *
     * @param uuid    UUID
     * @param rules   配置规则
     */
    void pushConfig(String uuid, String rules);

    /**
     * 推送命令到指定客户端
     *
     * @param reqVO 命令请求
     */
    void pushCommand(AgentCommandReqVO reqVO);

    /**
     * 获取 Agent 分页列表
     *
     * @param pageReqVO 分页查询
     * @return Agent 分页结果
     */
    PageResult<AgentRespVO> getAgentPage(AgentPageReqVO pageReqVO);

    /**
     * 获取 Agent 详情
     *
     * @param id Agent ID
     * @return Agent 详情
     */
    AgentRespVO getAgent(Long id);

}
