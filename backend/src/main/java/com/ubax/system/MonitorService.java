package com.ubax.system;

import com.ubax.app.repo.AppRepository;
import com.ubax.event.repo.EventRepository;
import com.ubax.pilot.repo.AgentRepository;
import com.ubax.profile.repo.UserProfileRepository;
import com.ubax.system.repo.AuditLogRepository;
import com.ubax.tenant.repo.UserRepository;
import org.springframework.boot.SpringBootVersion;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;

/** Builds the runtime/server/data snapshot for the 系统监控 page. */
@Service
public class MonitorService {

    private final OnlineUserRegistry onlineUserRegistry;
    private final EventRepository eventRepository;
    private final UserProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final AppRepository appRepository;
    private final AgentRepository agentRepository;
    private final AuditLogRepository auditLogRepository;

    public MonitorService(OnlineUserRegistry onlineUserRegistry, EventRepository eventRepository,
                          UserProfileRepository profileRepository, UserRepository userRepository,
                          AppRepository appRepository, AgentRepository agentRepository,
                          AuditLogRepository auditLogRepository) {
        this.onlineUserRegistry = onlineUserRegistry;
        this.eventRepository = eventRepository;
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.appRepository = appRepository;
        this.agentRepository = agentRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public Map<String, Object> snapshot(Long tid) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("server", server());
        root.put("jvm", jvm());
        root.put("data", data(tid));
        root.put("onlineUsers", onlineUserRegistry.list());
        root.put("onlineCount", onlineUserRegistry.count());
        root.put("timestamp", LocalDateTime.now().toString());
        return root;
    }

    private Map<String, Object> server() {
        Runtime rt = Runtime.getRuntime();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("osName", System.getProperty("os.name"));
        m.put("osArch", System.getProperty("os.arch"));
        m.put("osVersion", System.getProperty("os.version"));
        m.put("cpuCores", rt.availableProcessors());
        m.put("hostName", System.getenv().getOrDefault("COMPUTERNAME",
                System.getenv().getOrDefault("HOSTNAME", "localhost")));
        m.put("jvmName", ManagementFactory.getRuntimeMXBean().getVmName());
        return m;
    }

    private Map<String, Object> jvm() {
        MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = mem.getHeapMemoryUsage();
        RuntimeMXBean rt = ManagementFactory.getRuntimeMXBean();
        long uptime = rt.getUptime();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("javaVersion", System.getProperty("java.version"));
        m.put("springBoot", SpringBootVersion.getVersion());
        m.put("heapUsedMb", mb(heap.getUsed()));
        m.put("heapCommittedMb", mb(heap.getCommitted()));
        m.put("heapMaxMb", mb(heap.getMax()));
        m.put("heapUsagePct", heap.getMax() > 0 ? Math.round(heap.getUsed() * 1000.0 / heap.getMax()) / 10.0 : 0);
        m.put("nonHeapUsedMb", mb(mem.getNonHeapMemoryUsage().getUsed()));
        m.put("startTime", LocalDateTime.ofInstant(Instant.ofEpochMilli(rt.getStartTime()), ZoneId.systemDefault()).toString());
        m.put("uptime", humanDuration(Duration.ofMillis(uptime)));
        m.put("uptimeMs", uptime);
        return m;
    }

    private Map<String, Object> data(Long tid) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("events", tid != null ? eventRepository.countByTenantId(tid) : eventRepository.count());
        m.put("profiles", tid != null ? profileRepository.countByTenantId(tid) : profileRepository.count());
        m.put("users", userRepository.count());
        m.put("apps", appRepository.count());
        m.put("agents", agentRepository.count());
        m.put("auditLogs", tid != null ? auditLogRepository.countByTenantId(tid) : auditLogRepository.count());
        m.put("scoped", tid != null);
        return m;
    }

    private static long mb(long bytes) {
        return Math.round(bytes / 1024.0 / 1024.0);
    }

    private static String humanDuration(Duration d) {
        long days = d.toDays();
        long hours = d.toHoursPart();
        long mins = d.toMinutesPart();
        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("天");
        if (hours > 0 || days > 0) sb.append(hours).append("小时");
        sb.append(mins).append("分");
        return sb.toString();
    }
}
