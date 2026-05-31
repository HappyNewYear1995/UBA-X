package com.huanniankj.module.agent.controller.agent.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Agent心跳 Request VO
 *
 * @author zhaoff
 */
@Schema(description = "Agent心跳 Request VO")
@Setter
@Getter
public class AgentHeartbeatReqVO {

    @JsonProperty("uuid")
    private String uuid;

    @JsonProperty("hostname")
    private String hostname;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("collector_status")
    private String collectorStatus;

    @Override
    public String toString() {
        return String.format("Heartbeat{uuid='%s', hostname='%s', status='%s'}", uuid, hostname, collectorStatus);
    }

}
