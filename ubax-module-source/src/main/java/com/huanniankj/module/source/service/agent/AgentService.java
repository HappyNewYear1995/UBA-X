package com.huanniankj.module.source.service.agent;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.source.controller.agent.vo.*;
import com.huanniankj.module.source.controller.agent.vo.*;

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
     * 接收心跳上报
     *
     * @param reqVO 心跳数据
     */
    void receiveHeartbeat(AgentHeartbeatReqVO reqVO);

    /**
     * 推送配置到指定客户端
     *
     * @param uuid    UUID
     */
    void pushConfig(String uuid);

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

    /**
     * 更新 Agent 状态
     *
     * @param id     Agent ID
     * @param status 状态值
     */
    void updateAgentStatus(Long id, Integer status);

    /**
     * 更新 Agent 信息
     *
     * @param updateReqVO 更新请求
     */
    void updateAgent(AgentUpdateReqVO updateReqVO);

}
