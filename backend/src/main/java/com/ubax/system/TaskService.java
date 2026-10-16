package com.ubax.system;

import com.ubax.common.BusinessException;
import com.ubax.event.repo.EventRepository;
import com.ubax.system.domain.ScheduledTask;
import com.ubax.system.domain.TaskLog;
import com.ubax.system.repo.AuditLogRepository;
import com.ubax.system.repo.ScheduledTaskRepository;
import com.ubax.system.repo.TaskLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Built-in maintenance jobs. Each task has a real handler; runs are recorded in
 * {@link TaskLog}. A fixed-delay dispatcher auto-runs enabled tasks on their
 * effective interval (cron string is the human-readable equivalent).
 */
@Service
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    /** Effective auto-run interval per task code, in minutes. */
    private static final Map<String, Long> INTERVAL_MIN = Map.of(
            "clean_expired_events", 1440L,   // daily
            "clean_audit_logs", 1440L,       // daily
            "refresh_monitor", 5L            // every 5 min
    );

    private final ScheduledTaskRepository taskRepo;
    private final TaskLogRepository logRepo;
    private final EventRepository eventRepository;
    private final AuditLogRepository auditLogRepository;
    private final SystemService systemService;
    private final MonitorService monitorService;

    public TaskService(ScheduledTaskRepository taskRepo, TaskLogRepository logRepo,
                       EventRepository eventRepository, AuditLogRepository auditLogRepository,
                       SystemService systemService, MonitorService monitorService) {
        this.taskRepo = taskRepo;
        this.logRepo = logRepo;
        this.eventRepository = eventRepository;
        this.auditLogRepository = auditLogRepository;
        this.systemService = systemService;
        this.monitorService = monitorService;
    }

    public List<ScheduledTask> list() {
        return taskRepo.findAllByOrderByIdAsc();
    }

    public List<TaskLog> logs(String code, int limit) {
        PageRequest pr = PageRequest.of(0, Math.max(1, Math.min(limit, 500)));
        return (code == null || code.isBlank())
                ? logRepo.findAllByOrderByStartAtDesc(pr)
                : logRepo.findByTaskCodeOrderByStartAtDesc(code, pr);
    }

    public ScheduledTask toggle(Long id, boolean enabled) {
        ScheduledTask t = taskRepo.findById(id).orElseThrow(() -> new BusinessException(404, "任务不存在"));
        t.setEnabled(enabled);
        return taskRepo.save(t);
    }

    /** Manual trigger from the UI. */
    @Transactional
    public TaskLog runNow(Long id) {
        ScheduledTask t = taskRepo.findById(id).orElseThrow(() -> new BusinessException(404, "任务不存在"));
        return execute(t, "MANUAL");
    }

    @Transactional
    public TaskLog runByCode(String code, String trigger) {
        ScheduledTask t = taskRepo.findByCode(code).orElse(null);
        if (t == null || !t.isEnabled()) return null;
        return execute(t, trigger);
    }

    /** Dispatcher: auto-run enabled tasks whose interval has elapsed. */
    @Scheduled(initialDelay = 90_000, fixedDelay = 60_000)
    @Transactional
    public void dispatch() {
        for (ScheduledTask t : taskRepo.findByEnabledTrue()) {
            long interval = INTERVAL_MIN.getOrDefault(t.getCode(), 1440L);
            LocalDateTime last = t.getLastRunAt();
            if (last == null || last.isBefore(LocalDateTime.now().minusMinutes(interval))) {
                try {
                    execute(t, "AUTO");
                } catch (Exception e) {
                    log.warn("Scheduled task {} failed: {}", t.getCode(), e.getMessage());
                }
            }
        }
    }

    private TaskLog execute(ScheduledTask t, String trigger) {
        LocalDateTime start = LocalDateTime.now();
        TaskLog tl = new TaskLog();
        tl.setTaskCode(t.getCode());
        tl.setTaskName(t.getName());
        tl.setStartAt(start);
        tl.setTriggerType(trigger);
        String status;
        String message;
        long affected = 0;
        try {
            long[] res = handle(t.getCode());
            affected = res[0];
            message = codeMessage(t.getCode(), affected);
            status = "SUCCESS";
        } catch (Exception e) {
            status = "FAIL";
            message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        }
        LocalDateTime end = LocalDateTime.now();
        tl.setEndAt(end);
        tl.setStatus(status);
        tl.setMessage(message);
        tl.setAffected(affected);
        logRepo.save(tl);

        t.setLastRunAt(end);
        t.setLastStatus(status);
        t.setLastMessage(message);
        t.setRunCount(t.getRunCount() + 1);
        taskRepo.save(t);
        return tl;
    }

    /** Runs the real work for a task code; returns affected-row count. */
    private long[] handle(String code) {
        switch (code) {
            case "clean_expired_events": {
                int days = systemService.getInt("data.retention.days", 180);
                LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
                int n = eventRepository.deleteBefore(cutoff);
                return new long[]{n};
            }
            case "clean_audit_logs": {
                int days = systemService.getInt("audit.retention.days", 90);
                LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
                int n = auditLogRepository.deleteBefore(cutoff);
                return new long[]{n};
            }
            case "refresh_monitor": {
                monitorService.snapshot(null);
                return new long[]{0};
            }
            default:
                return new long[]{0};
        }
    }

    private String codeMessage(String code, long affected) {
        switch (code) {
            case "clean_expired_events":
                return "已清理超过 " + systemService.getInt("data.retention.days", 180) + " 天的事件 " + affected + " 条";
            case "clean_audit_logs":
                return "已清理超过 " + systemService.getInt("audit.retention.days", 90) + " 天的日志 " + affected + " 条";
            case "refresh_monitor":
                return "监控快照刷新完成";
            default:
                return "执行完成，影响 " + affected + " 行";
        }
    }
}
