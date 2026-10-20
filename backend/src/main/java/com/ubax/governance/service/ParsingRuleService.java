package com.ubax.governance.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubax.common.BusinessException;
import com.ubax.collector.dto.EventPayload;
import com.ubax.governance.domain.ParsingRule;
import com.ubax.governance.repo.ParsingRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Config-driven transformation applied to an incoming payload before it becomes
 * an Event. Rules run in priority order within a tenant/app scope and support
 * set / rename / extract / drop / anonymize actions. Returns null to signal a
 * drop.
 */
@Service
public class ParsingRuleService {

    private static final Logger log = LoggerFactory.getLogger(ParsingRuleService.class);

    private final ParsingRuleRepository repo;
    private final ObjectMapper objectMapper;
    private final Map<Long, List<Compiled>> cache = new ConcurrentHashMap<>();

    public ParsingRuleService(ParsingRuleRepository repo, ObjectMapper objectMapper) {
        this.repo = repo;
        this.objectMapper = objectMapper;
    }

    private record Compiled(long appId, Map<String, Object> match, Map<String, Object> action) {}

    // ---------- hot path ----------

    private List<Compiled> rules(Long tenantId) {
        return cache.computeIfAbsent(tenantId, t -> {
            List<Compiled> list = new ArrayList<>();
            for (ParsingRule r : repo.findByTenantIdAndEnabledTrueOrderByPriorityAscIdAsc(t)) {
                list.add(new Compiled(r.getAppId() == null ? 0L : r.getAppId(),
                        json(r.getMatchExpr()), json(r.getAction())));
            }
            return list;
        });
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> json(String s) {
        if (s == null || s.isBlank()) return Collections.emptyMap();
        try {
            return objectMapper.readValue(s, Map.class);
        } catch (Exception e) {
            log.debug("parsing rule json invalid: {}", s);
            return Collections.emptyMap();
        }
    }

    /** Apply all matching rules for the tenant/app. Returns null if any rule drops the event. */
    public EventPayload apply(Long tenantId, Long appId, EventPayload p) {
        long aid = appId == null ? 0L : appId;
        for (Compiled c : rules(tenantId)) {
            if (c.appId() != 0L && c.appId() != aid) continue;   // app-specific rules only match their app
            if (!matches(c.match(), p)) continue;
            p = runAction(c.action(), p);
            if (p == null) return null;                          // dropped
        }
        return p;
    }

    private boolean matches(Map<String, Object> match, EventPayload p) {
        if (match.isEmpty()) return true;                        // empty match = wildcard
        for (Map.Entry<String, Object> e : match.entrySet()) {
            String actual = fieldValue(p, e.getKey());
            if (!valueEquals(actual, e.getValue())) return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private boolean valueEquals(String actual, Object expected) {
        if (expected == null) return actual == null;
        if (expected instanceof List<?> l) {
            for (Object o : l) if (Objects.equals(actual, String.valueOf(o))) return true;
            return false;
        }
        if (expected instanceof Map<?, ?> m) {                   // {"contains":"x"} / {"prefix":"y"}
            String s = actual == null ? "" : actual;
            if (m.containsKey("contains") && !s.contains(String.valueOf(m.get("contains")))) return false;
            if (m.containsKey("prefix") && !s.startsWith(String.valueOf(m.get("prefix")))) return false;
            if (m.containsKey("equals") && !s.equals(String.valueOf(m.get("equals")))) return false;
            return true;
        }
        return Objects.equals(actual, String.valueOf(expected));
    }

    private EventPayload runAction(Map<String, Object> action, EventPayload p) {
        if (action.isEmpty()) return p;
        String op = String.valueOf(action.getOrDefault("op", "set"));
        switch (op) {
            case "drop":
                return null;
            case "set":
            case "rename":
                setField(p, str(action.get("field")), action.get("value"));
                return p;
            case "extract": {
                String from = str(action.get("from"));            // e.g. properties.plan
                String to = str(action.get("to"));
                Object v = fieldValueRaw(p, from);
                if (v != null) setField(p, to, v);
                return p;
            }
            case "anonymize": {
                String field = str(action.get("field"));
                String cur = fieldValue(p, field);
                if (cur != null && cur.length() > 4) setField(p, field, cur.substring(0, 4) + "****");
                return p;
            }
            default:
                return p;
        }
    }

    private static String str(Object o) { return o == null ? null : String.valueOf(o); }

    /** Read a (possibly nested properties.*) value as String for matching. */
    private String fieldValue(EventPayload p, String key) {
        Object v = fieldValueRaw(p, key);
        return v == null ? null : String.valueOf(v);
    }

    private Object fieldValueRaw(EventPayload p, String key) {
        if (key == null) return null;
        if (key.startsWith("properties.")) {
            String pk = key.substring("properties.".length());
            return p.getProperties() == null ? null : p.getProperties().get(pk);
        }
        return switch (key) {
            case "event" -> p.getEvent();
            case "type" -> p.getType();
            case "distinctId" -> p.getDistinctId();
            case "userId" -> p.getUserId();
            case "url" -> p.getUrl();
            case "path" -> p.getPath();
            case "referrer" -> p.getReferrer();
            case "title" -> p.getTitle();
            case "screen" -> p.getScreen();
            case "element" -> p.getElement();
            case "country" -> p.getCountry();
            case "region" -> p.getRegion();
            case "city" -> p.getCity();
            case "appKey" -> p.getAppKey();
            default -> null;
        };
    }

    @SuppressWarnings("unchecked")
    private void setField(EventPayload p, String key, Object value) {
        if (key == null) return;
        if (key.startsWith("properties.")) {
            String pk = key.substring("properties.".length());
            Map<String, Object> props = p.getProperties();
            if (props == null) { props = new LinkedHashMap<>(); p.setProperties(props); }
            props.put(pk, value);
            return;
        }
        String s = value == null ? null : String.valueOf(value);
        switch (key) {
            case "event" -> p.setEvent(s);
            case "type" -> p.setType(s);
            case "distinctId" -> p.setDistinctId(s);
            case "userId" -> p.setUserId(s);
            case "url" -> p.setUrl(s);
            case "path" -> p.setPath(s);
            case "referrer" -> p.setReferrer(s);
            case "title" -> p.setTitle(s);
            case "screen" -> p.setScreen(s);
            case "element" -> p.setElement(s);
            case "country" -> p.setCountry(s);
            case "region" -> p.setRegion(s);
            case "city" -> p.setCity(s);
            default -> log.debug("parsing rule set: unknown field {}", key);
        }
    }

    public void invalidate(Long tenantId) {
        if (tenantId == null) cache.clear();
        else cache.remove(tenantId);
    }

    // ---------- management ----------

    public Map<String, Object> page(Long tenantId, Long appId, int page, int size) {
        PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "priority").and(Sort.by(Sort.Direction.DESC, "id")));
        Page<ParsingRule> p = (appId == null)
                ? repo.findByTenantId(tenantId, pr)
                : repo.findByTenantIdAndAppId(tenantId, appId, pr);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("rows", p.getContent());
        m.put("total", p.getTotalElements());
        m.put("page", page);
        m.put("size", size);
        return m;
    }

    public ParsingRule save(ParsingRule r, Long tenantId) {
        r.setTenantId(tenantId);
        if (r.getAppId() == null) r.setAppId(0L);
        validateJson("matchExpr", r.getMatchExpr());
        validateJson("action", r.getAction());
        ParsingRule saved = repo.save(r);
        invalidate(tenantId);
        return saved;
    }

    private void validateJson(String field, String s) {
        if (s == null || s.isBlank()) return;
        try {
            objectMapper.readValue(s, Map.class);
        } catch (Exception e) {
            throw new BusinessException(field + " 不是合法 JSON");
        }
    }

    public ParsingRule toggle(Long id, boolean enabled, Long tenantId) {
        ParsingRule r = repo.findById(id).orElseThrow(() -> new BusinessException(404, "规则不存在"));
        r.setEnabled(enabled);
        ParsingRule saved = repo.save(r);
        invalidate(tenantId);
        return saved;
    }

    public void delete(Long id, Long tenantId) {
        repo.deleteById(id);
        invalidate(tenantId);
    }
}
