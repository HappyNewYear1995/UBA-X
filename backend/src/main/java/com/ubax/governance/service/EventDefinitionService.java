package com.ubax.governance.service;

import com.ubax.common.BusinessException;
import com.ubax.governance.domain.EventDefinition;
import com.ubax.governance.repo.EventDefinitionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Event dictionary: resolves an incoming event key against the tenant/app
 * definitions and decides what to do on a miss. Definitions are cached per
 * tenant because resolution runs on the per-event hot path.
 */
@Service
public class EventDefinitionService {

    /** Result of resolving an event key. null return means "drop this event". */
    public record Resolved(String eventName, String type, boolean undefined) {}

    private final EventDefinitionRepository repo;
    /** tenantId -> (eventKey -> definition), including appId=0 fallbacks. */
    private final Map<Long, Map<String, EventDefinition>> cache = new ConcurrentHashMap<>();

    public EventDefinitionService(EventDefinitionRepository repo) {
        this.repo = repo;
    }

    // ---------- hot path ----------

    private Map<String, EventDefinition> defs(Long tenantId) {
        return cache.computeIfAbsent(tenantId, t -> {
            Map<String, EventDefinition> m = new ConcurrentHashMap<>();
            for (EventDefinition d : repo.findByTenantIdAndEnabledTrue(t)) {
                if (d.getEventKey() == null) continue;
                // app-specific wins over appId=0; keep first-seen for the same key+scope
                m.putIfAbsent(scopeKey(d.getAppId(), d.getEventKey()), d);
            }
            return m;
        });
    }

    private static String scopeKey(Long appId, String eventKey) {
        return (appId == null ? 0L : appId) + "|" + eventKey;
    }

    /**
     * Resolve an event key for a tenant/app. Returns the standardized name/type
     * when a definition matches; otherwise applies the tenant's on-miss policy
     * (default MARK). A null return signals the caller to drop the event.
     */
    public Resolved resolve(Long tenantId, Long appId, String eventKey, String fallbackType) {
        if (eventKey == null || eventKey.isBlank()) {
            return new Resolved(eventKey, fallbackType, false);
        }
        Map<String, EventDefinition> m = defs(tenantId);
        EventDefinition def = m.get(scopeKey(appId, eventKey));
        if (def == null) def = m.get(scopeKey(0L, eventKey));

        if (def != null) {
            String name = def.getEventName() != null && !def.getEventName().isBlank()
                    ? def.getEventName() : eventKey;
            String type = def.getType() != null && !def.getType().isBlank() ? def.getType() : fallbackType;
            return new Resolved(name, type, false);
        }
        // No definition: fall back to the tenant-wide on-miss policy (default MARK).
        String onMiss = m.values().stream()
                .filter(d -> d.getAppId() != null && d.getAppId() == 0L)
                .map(EventDefinition::getOnMiss)
                .findFirst().orElse("MARK");
        return switch (onMiss == null ? "MARK" : onMiss) {
            case "DROP" -> null;
            case "UNKNOWN" -> new Resolved("$unknown", "custom", true);
            default -> new Resolved(eventKey, fallbackType, true);
        };
    }

    public void invalidate(Long tenantId) {
        if (tenantId == null) cache.clear();
        else cache.remove(tenantId);
    }

    // ---------- management ----------

    public Map<String, Object> page(Long tenantId, Long appId, int page, int size) {
        PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<EventDefinition> p = (appId == null)
                ? repo.findByTenantId(tenantId, pr)
                : repo.findByTenantIdAndAppId(tenantId, appId, pr);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("rows", p.getContent());
        m.put("total", p.getTotalElements());
        m.put("page", page);
        m.put("size", size);
        return m;
    }

    public EventDefinition save(EventDefinition d, Long tenantId) {
        if (d.getEventKey() == null || d.getEventKey().isBlank()) throw new BusinessException("事件键必填");
        d.setTenantId(tenantId);
        if (d.getAppId() == null) d.setAppId(0L);
        if (d.getId() == null
                && repo.existsByTenantIdAndAppIdAndEventKey(tenantId, d.getAppId(), d.getEventKey())) {
            throw new BusinessException("该应用下事件键已存在");
        }
        EventDefinition saved = repo.save(d);
        invalidate(tenantId);
        return saved;
    }

    public EventDefinition toggle(Long id, boolean enabled, Long tenantId) {
        EventDefinition d = repo.findById(id).orElseThrow(() -> new BusinessException(404, "事件定义不存在"));
        d.setEnabled(enabled);
        EventDefinition saved = repo.save(d);
        invalidate(tenantId);
        return saved;
    }

    public void delete(Long id, Long tenantId) {
        repo.deleteById(id);
        invalidate(tenantId);
    }

    public List<EventDefinition> all(Long tenantId) {
        return repo.findByTenantIdAndEnabledTrue(tenantId);
    }
}
