package com.ubax.pilot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubax.app.domain.App;
import com.ubax.app.repo.AppRepository;
import com.ubax.common.BusinessException;
import com.ubax.common.TenantContext;
import com.ubax.pilot.domain.Agent;
import com.ubax.pilot.domain.AgentCommand;
import com.ubax.pilot.repo.AgentCommandRepository;
import com.ubax.pilot.repo.AgentRepository;
import com.ubax.system.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class PilotService {

    private static final Logger log = LoggerFactory.getLogger(PilotService.class);
    private static final long OFFLINE_AFTER_SECONDS = 90;

    private final AgentRepository agentRepository;
    private final AgentCommandRepository commandRepository;
    private final AppRepository appRepository;
    private final ObjectMapper objectMapper;

    public PilotService(AgentRepository agentRepository, AgentCommandRepository commandRepository,
                        AppRepository appRepository, ObjectMapper objectMapper) {
        this.agentRepository = agentRepository;
        this.commandRepository = commandRepository;
        this.appRepository = appRepository;
        this.objectMapper = objectMapper;
    }

    // ---------- management plane (authenticated) ----------

    public List<Agent> listAgents() {
        return agentRepository.findByTenantIdOrderByIdDesc(TenantContext.requireTenantId());
    }

    public Map<String, Object> overview() {
        Long tid = TenantContext.requireTenantId();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("total", agentRepository.countByTenantId(tid));
        m.put("online", agentRepository.countByTenantIdAndStatus(tid, "ONLINE"));
        m.put("offline", agentRepository.countByTenantIdAndStatus(tid, "OFFLINE"));
        m.put("installing", agentRepository.countByTenantIdAndStatus(tid, "INSTALLING"));
        return m;
    }

    @Transactional
    public Map<String, Object> createAgent(String name, String host, String tags, Map<String, Object> config) {
        Long tid = TenantContext.requireTenantId();
        Agent a = new Agent();
        a.setTenantId(tid);
        a.setName(name);
        a.setHost(host);
        a.setTags(tags);
        a.setAgentKey("pilot_" + UUID.randomUUID().toString().replace("-", ""));
        a.setStatus("INSTALLING");
        Map<String, Object> cfg = config != null ? new LinkedHashMap<>(config) : defaultConfig();
        if (isBlank(str(cfg.get("appKey")))) {
            String ak = defaultAppKey(tid);
            if (ak != null) cfg.put("appKey", ak);
        }
        try {
            a.setConfig(objectMapper.writeValueAsString(cfg));
        } catch (Exception e) {
            a.setConfig("{}");
        }
        a.setConfigVersion(1L);
        agentRepository.save(a);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("agent", a);
        out.put("installCommand", installCommand(a));
        return out;
    }

    private Map<String, Object> defaultConfig() {
        Map<String, Object> c = new LinkedHashMap<>();
        c.put("collectIntervalMs", 5000);
        c.put("batchSize", 50);
        c.put("endpoints", List.of("/api/collect/batch"));
        c.put("sampling", 1.0);
        c.put("enabled", true);
        return c;
    }

    /** First ACTIVE app key of the tenant, used so a freshly installed probe can collect immediately. */
    private String defaultAppKey(Long tenantId) {
        if (tenantId == null) return null;
        return appRepository.findByTenantIdOrderByIdDesc(tenantId).stream()
                .filter(ap -> "ACTIVE".equals(ap.getStatus()))
                .map(App::getAppKey)
                .findFirst()
                .orElse(null);
    }

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }

    /**
     * Ensure the agent's stored config carries an appKey (backfilled from the tenant's
     * first active app). Returns the config as a Map for the register/heartbeat response.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> configWithAppKey(Agent a) {
        Map<String, Object> cfg;
        try {
            cfg = isBlank(a.getConfig()) ? new LinkedHashMap<>()
                    : objectMapper.readValue(a.getConfig(), Map.class);
        } catch (Exception e) {
            cfg = new LinkedHashMap<>();
        }
        if (isBlank(str(cfg.get("appKey")))) {
            String ak = defaultAppKey(a.getTenantId());
            if (ak != null) {
                cfg.put("appKey", ak);
                try {
                    a.setConfig(objectMapper.writeValueAsString(cfg));
                    agentRepository.save(a);
                } catch (Exception ignored) {}
            }
        }
        return cfg;
    }

    private String installCommand(Agent a) {
        return "curl -fsSL http://<SERVER>:8081/sdk/pilot-agent.sh | " +
                "bash -s -- --key " + a.getAgentKey() + " --server http://<SERVER>:8081 --name '" + a.getName() + "'";
    }

    @Transactional
    public Agent updateConfig(Long id, Map<String, Object> config) {
        Agent a = getAgent(id);
        try {
            a.setConfig(objectMapper.writeValueAsString(config));
        } catch (Exception e) {
            throw new BusinessException("配置格式错误");
        }
        a.setConfigVersion(a.getConfigVersion() + 1);
        agentRepository.save(a);
        enqueue(a, "RELOAD_CONFIG", a.getConfig());
        return a;
    }

    @Transactional
    public AgentCommand sendCommand(Long id, String type, String payload) {
        Agent a = getAgent(id);
        return enqueue(a, type, payload);
    }

    private AgentCommand enqueue(Agent a, String type, String payload) {
        AgentCommand c = new AgentCommand();
        c.setTenantId(a.getTenantId());
        c.setAgentId(a.getId());
        c.setType(type);
        c.setPayload(payload);
        c.setStatus("PENDING");
        return commandRepository.save(c);
    }

    public Agent getAgent(Long id) {
        return agentRepository.findByIdAndTenantId(id, TenantContext.requireTenantId())
                .orElseThrow(() -> new BusinessException(404, "探针不存在"));
    }

    public List<AgentCommand> commands(Long id) {
        getAgent(id);
        return commandRepository.findByAgentIdAndTenantIdOrderByIdDesc(id, TenantContext.requireTenantId());
    }

    @Transactional
    public void deleteAgent(Long id) {
        Agent a = getAgent(id);
        agentRepository.delete(a);
    }

    // ---------- agent plane (token authenticated by agentKey) ----------

    @Transactional
    public Map<String, Object> register(Map<String, Object> body, String remoteIp) {
        String key = str(body.get("agentKey"));
        Agent a = agentRepository.findByAgentKey(key)
                .orElseThrow(() -> new BusinessException(401, "无效的 agentKey"));
        a.setStatus("ONLINE");
        a.setLastHeartbeat(LocalDateTime.now());
        if (body.get("host") != null) a.setHost(str(body.get("host")));
        if (body.get("name") != null && !isBlank(str(body.get("name")))) a.setName(str(body.get("name")));
        if (body.get("tags") != null && !isBlank(str(body.get("tags")))) a.setTags(str(body.get("tags")));
        // 优先用探针自报 IP, 否则用服务端观测到的远端 IP (NAT 后仍可定位接入点)
        String ip = !isBlank(str(body.get("ip"))) ? str(body.get("ip")) : remoteIp;
        if (!isBlank(ip)) a.setIp(ip);
        if (body.get("version") != null) a.setVersion(str(body.get("version")));
        if (body.get("os") != null) a.setOs(str(body.get("os")));
        if (body.get("arch") != null) a.setArch(str(body.get("arch")));
        agentRepository.save(a);
        Map<String, Object> cfg = configWithAppKey(a);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("agentId", a.getId());
        out.put("name", a.getName());
        out.put("configVersion", a.getConfigVersion());
        out.put("config", cfg);
        return out;
    }

    @Transactional
    public Map<String, Object> heartbeat(Map<String, Object> body, String remoteIp) {
        String key = str(body.get("agentKey"));
        Agent a = agentRepository.findByAgentKey(key)
                .orElseThrow(() -> new BusinessException(401, "无效的 agentKey"));
        a.setStatus("ONLINE");
        a.setLastHeartbeat(LocalDateTime.now());
        if (isBlank(a.getIp()) && !isBlank(remoteIp)) a.setIp(remoteIp);
        if (body.get("metrics") != null) {
            try { a.setMetrics(objectMapper.writeValueAsString(body.get("metrics"))); } catch (Exception ignored) {}
        }
        if (body.get("eventsSent") != null) {
            a.setEventsSent(((Number) body.get("eventsSent")).longValue());
        }
        agentRepository.save(a);

        // deliver pending commands
        List<AgentCommand> pending = commandRepository.findByAgentIdAndStatusOrderByIdAsc(a.getId(), "PENDING");
        List<Map<String, Object>> cmds = new ArrayList<>();
        for (AgentCommand c : pending) {
            Map<String, Object> cm = new LinkedHashMap<>();
            cm.put("id", c.getId());
            cm.put("type", c.getType());
            cm.put("payload", c.getPayload());
            cmds.add(cm);
            c.setStatus("DELIVERED");
            c.setDeliveredAt(LocalDateTime.now());
            commandRepository.save(c);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("configVersion", a.getConfigVersion());
        out.put("config", configWithAppKey(a));
        out.put("commands", cmds);
        return out;
    }

    @Transactional
    public void reportResult(Map<String, Object> body) {
        String key = str(body.get("agentKey"));
        Agent a = agentRepository.findByAgentKey(key).orElse(null);
        if (a == null) return;
        Number cmdId = (Number) body.get("commandId");
        if (cmdId != null) {
            commandRepository.findById(cmdId.longValue()).ifPresent(c -> {
                c.setStatus(str(body.getOrDefault("status", "DONE")));
                c.setResult(str(body.get("result")));
                c.setFinishedAt(LocalDateTime.now());
                commandRepository.save(c);
            });
        }
    }

    private String str(Object o) { return o == null ? null : o.toString(); }

    // ---------- housekeeping ----------

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void markOffline() {
        LocalDateTime cutoff = LocalDateTime.now().minusSeconds(OFFLINE_AFTER_SECONDS);
        List<Agent> stale = agentRepository.findByStatusAndLastHeartbeatBefore("ONLINE", cutoff);
        for (Agent a : stale) {
            a.setStatus("OFFLINE");
            agentRepository.save(a);
        }
        if (!stale.isEmpty()) log.info("Marked {} agents offline", stale.size());
    }
}
