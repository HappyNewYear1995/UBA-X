package com.ubax.system.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/** Execution history entry for a scheduled task run. */
@Data
@Entity
@Table(name = "uba_sys_task_log", indexes = @Index(name = "idx_tasklog_code", columnList = "task_code"))
public class TaskLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_code", length = 60)
    private String taskCode;

    @Column(name = "task_name", length = 80)
    private String taskName;

    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;

    /** SUCCESS / FAIL */
    @Column(length = 16)
    private String status;

    @Column(length = 300)
    private String message;

    private Long affected;

    /** MANUAL (立即执行) / AUTO (调度触发) */
    @Column(name = "trigger_type", length = 16)
    private String triggerType = "MANUAL";
}
