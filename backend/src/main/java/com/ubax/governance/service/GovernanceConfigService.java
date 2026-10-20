package com.ubax.governance.service;

import com.ubax.system.SystemService;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Reads the platform-global {@code collect.*} switches that {@code DataSeeder}
 * seeds but the collector historically ignored. Values are cached for a short
 * TTL because this sits on the per-event hot path.
 */
@Service
public class GovernanceConfigService {

    private static final long TTL_MS = 60_000L;

    private final SystemService systemService;

    private final AtomicLong loadedAt = new AtomicLong(0);
    private final AtomicReference<Snapshot> snapshot = new AtomicReference<>(new Snapshot(true, 1.0, false));

    public GovernanceConfigService(SystemService systemService) {
        this.systemService = systemService;
    }

    private record Snapshot(boolean enabled, double sampling, boolean anonymize) {}

    private Snapshot current() {
        long now = System.currentTimeMillis();
        Snapshot s = snapshot.get();
        if (s == null || now - loadedAt.get() > TTL_MS) {
            boolean enabled = systemService.getBool("collect.enabled", true);
            double sampling = parseDouble(systemService.get("collect.sampling", "1.0"), 1.0);
            boolean anonymize = systemService.getBool("collect.ip.anonymize", false);
            s = new Snapshot(enabled, clamp01(sampling), anonymize);
            snapshot.set(s);
            loadedAt.set(now);
        }
        return s;
    }

    /** Force the next read to hit the database (called after a config write). */
    public void invalidate() {
        loadedAt.set(0);
    }

    public boolean collectEnabled() {
        return current().enabled();
    }

    public boolean ipAnonymize() {
        return current().anonymize();
    }

    /** Sampling gate: returns true when this event should be kept. */
    public boolean pass() {
        double s = current().sampling();
        if (s >= 1.0) return true;
        if (s <= 0.0) return false;
        return ThreadLocalRandom.current().nextDouble() < s;
    }

    public String maskIp(String ip) {
        if (ip == null || ip.isBlank()) return ip;
        if (ip.contains(".")) {                 // IPv4: zero the last octet
            int i = ip.lastIndexOf('.');
            return ip.substring(0, i + 1) + "0";
        }
        if (ip.contains(":")) {                 // IPv6: keep first 4 groups
            String[] parts = ip.split(":");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < parts.length && i < 4; i++) {
                if (i > 0) sb.append(':');
                sb.append(parts[i]);
            }
            return sb.append("::").toString();
        }
        return ip;
    }

    private static double parseDouble(String v, double def) {
        try { return Double.parseDouble(v.trim()); } catch (Exception e) { return def; }
    }

    private static double clamp01(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }
}
