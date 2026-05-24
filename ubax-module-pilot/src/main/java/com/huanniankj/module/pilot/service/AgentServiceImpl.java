package com.huanniankj.module.pilot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huanniankj.module.pilot.dal.model.HeartbeatPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Agent服务实现
 *
 * @author zhaoff
 */
@Service
@Slf4j
public class AgentServiceImpl implements AgentsService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 存储在线客户端的 SSE 连接
     */
    private final Map<String, SseEmitter> clients = new ConcurrentHashMap<>();

    /**
     * 存储客户端最新心跳
     */
    private final Map<String, HeartbeatPayload> heartbeats = new ConcurrentHashMap<>();

    public void updateHeartbeat(HeartbeatPayload payload) {
        heartbeats.put(payload.getHostname(), payload);
        log.info("收到心跳: {}", payload);
    }

    public void registerClient(String hostname, SseEmitter emitter) {
        clients.put(hostname, emitter);
        log.info("客户端已连接: {}", hostname);

        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("欢迎连接 UBAX-Pilot 推送服务"));
        } catch (IOException e) {
            log.error("发送欢迎消息失败: {}", e.getMessage());
        }
    }

    public void removeClient(String hostname) {
        clients.remove(hostname);
        log.info("客户端已断开: {}", hostname);
    }

    public boolean pushConfig(String hostname, String rules, String version) {
        SseEmitter emitter = clients.get(hostname);
        if (emitter == null) {
            log.warn("客户端不在线: {}", hostname);
            return false;
        }

        try {
            Map<String, Object> payload = Map.of(
                    "rules", rules,
                    "version", version
            );

            Map<String, Object> message = Map.of(
                    "type", "config",
                    "payload", payload,
                    "timestamp", System.currentTimeMillis() / 1000
            );

            emitter.send(SseEmitter.event()
                    .name("message")
                    .data(objectMapper.writeValueAsString(message)));

            log.info("配置已推送到 {}: version={}", hostname, version);
            return true;
        } catch (IOException e) {
            log.error("推送配置失败: {}", e.getMessage());
            clients.remove(hostname);
            return false;
        }
    }

    public boolean pushCommand(String hostname, String action) {
        SseEmitter emitter = clients.get(hostname);
        if (emitter == null) {
            log.warn("客户端不在线: {}", hostname);
            return false;
        }

        try {
            Map<String, Object> payload = Map.of(
                    "action", action,
                    "params", Map.of()
            );

            Map<String, Object> message = Map.of(
                    "type", "command",
                    "payload", payload,
                    "timestamp", System.currentTimeMillis() / 1000
            );

            emitter.send(SseEmitter.event()
                    .name("message")
                    .data(objectMapper.writeValueAsString(message)));

            log.info("命令已推送到 {}: action={}", hostname, action);
            return true;
        } catch (IOException e) {
            log.error("推送命令失败: {}", e.getMessage());
            clients.remove(hostname);
            return false;
        }
    }

    public Map<String, HeartbeatPayload> getAllClients() {
        return new ConcurrentHashMap<>(heartbeats);
    }

}
