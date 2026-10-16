package com.ubax.system;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory presence tracker. The JWT filter touches a user's entry on every
 * authenticated request; entries not seen within the window are considered offline.
 */
@Component
public class OnlineUserRegistry {

    public static class Entry {
        public Long userId;
        public String username;
        public Long tenantId;
        public String ip;
        public String location;
        public String userAgent;
        public long lastSeen;
    }

    private static final long WINDOW_MS = 30 * 60 * 1000L; // 30 minutes
    private final Map<Long, Entry> online = new ConcurrentHashMap<>();

    public void touch(Long userId, String username, Long tenantId, String ip, String location, String userAgent) {
        if (userId == null) return;
        Entry e = online.computeIfAbsent(userId, k -> new Entry());
        e.userId = userId;
        e.username = username;
        e.tenantId = tenantId;
        e.ip = ip;
        e.location = location;
        e.userAgent = userAgent;
        e.lastSeen = System.currentTimeMillis();
    }

    public void leave(Long userId) {
        if (userId != null) online.remove(userId);
    }

    public List<Map<String, Object>> list() {
        long cutoff = System.currentTimeMillis() - WINDOW_MS;
        List<Entry> active = new ArrayList<>();
        for (Entry e : online.values()) {
            if (e.lastSeen >= cutoff) active.add(e);
        }
        active.sort(Comparator.comparingLong((Entry e) -> e.lastSeen).reversed());
        List<Map<String, Object>> out = new ArrayList<>();
        for (Entry e : active) {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("userId", e.userId);
            m.put("username", e.username);
            m.put("tenantId", e.tenantId);
            m.put("ip", e.ip);
            m.put("location", e.location);
            m.put("browser", e.userAgent);
            m.put("lastSeen", LocalDateTime.now().toString());
            m.put("idleSeconds", (System.currentTimeMillis() - e.lastSeen) / 1000);
            out.add(m);
        }
        return out;
    }

    public int count() {
        return list().size();
    }
}
