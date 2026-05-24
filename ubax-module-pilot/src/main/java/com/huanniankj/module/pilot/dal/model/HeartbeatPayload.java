package com.huanniankj.module.pilot.dal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class HeartbeatPayload {

    @JsonProperty("version")
    private String version;

    @JsonProperty("hostname")
    private String hostname;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("healthy")
    private boolean healthy;

    @JsonProperty("collector_status")
    private String collectorStatus;

    @JsonProperty("os")
    private String os;

    @Override
    public String toString() {
        return String.format("Heartbeat{hostname='%s', version='%s', status='%s', os='%s'}",
                hostname, version, collectorStatus, os);
    }

}
