package com.ubax.collector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubax.app.domain.App;
import com.ubax.app.repo.AppRepository;
import com.ubax.collector.dto.EventPayload;
import com.ubax.common.UserAgentParser;
import com.ubax.event.domain.Event;
import com.ubax.governance.service.EventDefinitionService;
import com.ubax.governance.service.GovernanceConfigService;
import com.ubax.governance.service.IdentityService;
import com.ubax.governance.service.ParsingRuleService;
import com.ubax.stream.StreamEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CollectorService {

    private static final Logger log = LoggerFactory.getLogger(CollectorService.class);
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    private final AppRepository appRepository;
    private final StreamEngine streamEngine;
    private final ObjectMapper objectMapper;
    private final GovernanceConfigService governanceConfig;
    private final ParsingRuleService parsingRuleService;
    private final EventDefinitionService eventDefinitionService;
    private final IdentityService identityService;
    /** appKey -> App cache to avoid a DB hit per event */
    private final Map<String, App> appCache = new ConcurrentHashMap<>();

    public CollectorService(AppRepository appRepository, StreamEngine streamEngine, ObjectMapper objectMapper,
                            GovernanceConfigService governanceConfig, ParsingRuleService parsingRuleService,
                            EventDefinitionService eventDefinitionService, IdentityService identityService) {
        this.appRepository = appRepository;
        this.streamEngine = streamEngine;
        this.objectMapper = objectMapper;
        this.governanceConfig = governanceConfig;
        this.parsingRuleService = parsingRuleService;
        this.eventDefinitionService = eventDefinitionService;
        this.identityService = identityService;
    }

    public App resolveApp(String appKey) {
        if (appKey == null || appKey.isBlank()) return null;
        App cached = appCache.get(appKey);
        if (cached != null) return cached;
        App app = appRepository.findByAppKey(appKey).orElse(null);
        if (app != null) appCache.put(appKey, app);
        return app;
    }

    public void evictCache() { appCache.clear(); }

    /**
     * Convert a wire payload into a persisted Event and hand it to the stream engine.
     * Returns true if accepted (queued), false if dropped/rejected.
     */
    public boolean ingest(EventPayload p, String ip, String userAgent) {
        App app = resolveApp(p.getAppKey());
        if (app == null || !"ACTIVE".equals(app.getStatus())) {
            log.debug("reject event: unknown/inactive appKey={}", p.getAppKey());
            return false;
        }
        Long tenantId = app.getTenantId();
        Long appId = app.getId();

        // ② governance switches (collect.enabled / sampling / ip.anonymize) — previously seeded but never read
        if (!governanceConfig.collectEnabled()) return false;
        if (!governanceConfig.pass()) return false;
        if (governanceConfig.ipAnonymize()) ip = governanceConfig.maskIp(ip);

        // ③ parsing rules (field mapping / extraction / filter / routing); null => dropped
        p = parsingRuleService.apply(tenantId, appId, p);
        if (p == null) return false;

        Event e = new Event();
        e.setTenantId(tenantId);
        e.setAppId(appId);
        String type = p.getType() == null ? "event" : p.getType();

        // ④ event dictionary: standardize name/type on hit; apply on-miss policy otherwise (null => dropped)
        EventDefinitionService.Resolved resolved =
                eventDefinitionService.resolve(tenantId, appId, p.getEvent(), type);
        if (resolved == null) return false;
        e.setEventType(resolved.type() != null ? resolved.type() : type);
        e.setEventName(resolved.eventName() != null ? resolved.eventName() : defaultName(type));

        e.setDistinctId(orDefault(p.getDistinctId(), "anon-" + app.getAppKey()));
        e.setUserId(p.getUserId());
        e.setSessionId(orDefault(p.getSessionId(), UUID.randomUUID().toString()));
        e.setUrl(trunc(p.getUrl(), 1000));
        e.setUrlPath(trunc(p.getPath(), 500));
        e.setReferrer(trunc(p.getReferrer(), 500));
        e.setTitle(trunc(p.getTitle(), 300));
        e.setScreen(trunc(p.getScreen(), 32));
        e.setX(p.getX());
        e.setY(p.getY());
        e.setScrollX(p.getScrollX());
        e.setScrollY(p.getScrollY());
        e.setVpW(p.getVpW());
        e.setVpH(p.getVpH());
        e.setDocW(p.getDocW());
        e.setDocH(p.getDocH());
        e.setElement(trunc(p.getElement(), 300));
        e.setDuration(p.getDuration());
        e.setIp(ip);
        e.setUserAgent(trunc(userAgent, 500));

        Map<String, String> ua = UserAgentParser.parse(userAgent);
        e.setDevice(ua.get("device"));
        e.setOs(ua.get("os"));
        e.setBrowser(ua.get("browser"));

        e.setCountry(orDefault(p.getCountry(), "中国"));
        e.setRegion(p.getRegion());
        e.setCity(p.getCity());

        if (p.getProperties() != null && !p.getProperties().isEmpty()) {
            try {
                e.setProperties(objectMapper.writeValueAsString(p.getProperties()));
            } catch (Exception ignored) {}
        }

        if (p.getTs() != null && p.getTs() > 0) {
            e.setEventTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(p.getTs()), ZONE));
        } else {
            e.setEventTime(LocalDateTime.now(ZONE));
        }

        // ⑤ identity resolution: fold anonymous distinctId onto a unified identity
        e.setDistinctId(identityService.resolve(tenantId, appId, e.getDistinctId(), p.getUserId()));

        return streamEngine.submit(e);
    }

    private String defaultName(String type) {
        return switch (type) {
            case "pageview" -> "$pageview";
            case "click" -> "$click";
            case "leave" -> "$leave";
            default -> "$event";
        };
    }

    private String orDefault(String v, String d) { return (v == null || v.isBlank()) ? d : v; }

    private String trunc(String v, int max) {
        if (v == null) return null;
        return v.length() <= max ? v : v.substring(0, max);
    }
}
