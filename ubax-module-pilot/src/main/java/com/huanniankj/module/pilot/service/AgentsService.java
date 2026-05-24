package com.huanniankj.module.pilot.service;

import com.huanniankj.module.pilot.dal.model.HeartbeatPayload;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

/**
 * Agent服务接口
 *
 * @author zhaoff
 */
public interface AgentsService {

    void updateHeartbeat(HeartbeatPayload payload);

    /**
     * 注册 SSE 推送客户端
     */
    void registerClient(String hostname, SseEmitter emitter);

    /**
     * 移除客户端
     */
    void removeClient(String hostname);

    /**
     * 推送配置到指定客户端
     */
    boolean pushConfig(String hostname, String rules, String version);

    /**
     * 推送命令到指定客户端
     */
    boolean pushCommand(String hostname, String action);

    /**
     * 获取所有在线客户端
     */
    Map<String, HeartbeatPayload> getAllClients();

}
