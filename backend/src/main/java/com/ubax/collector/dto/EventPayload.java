package com.ubax.collector.dto;

import lombok.Data;

import java.util.Map;

/**
 * Wire format sent by the collection SDK for a single behavior event.
 */
@Data
public class EventPayload {
    /** app key identifying the reporting application */
    private String appKey;
    /** pageview | click | event | custom | leave */
    private String type;
    private String event;
    private String distinctId;
    private String userId;
    private String sessionId;
    private String url;
    private String path;
    private String referrer;
    private String title;
    private String screen;
    private Double x;
    private Double y;
    /** page geometry captured with clicks so they can be re-projected onto the
     *  full document (clientX/Y alone are viewport-relative). */
    private Integer scrollX;
    private Integer scrollY;
    private Integer vpW;
    private Integer vpH;
    private Integer docW;
    private Integer docH;
    private String element;
    private Long duration;
    /** client-side timestamp (epoch millis) */
    private Long ts;
    /** geo provided by SDK (optional) */
    private String country;
    private String region;
    private String city;
    private Map<String, Object> properties;
}
