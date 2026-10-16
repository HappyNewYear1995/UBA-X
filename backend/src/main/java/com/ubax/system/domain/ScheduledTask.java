package com.ubax.system.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/** Definition + runtime state of a built-in scheduled job. */
@Data
@Entity
@Table(name = "uba_sys_task")
public class ScheduledTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String code;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(length = 40)
    private String cron;

    @Column(nullable = false)
    private boolean enabled = false;

    @Column(length = 200)
    private String description;

    @Column(name = "last_run_at")
    private LocalDateTime lastRunAt;

    @Column(name = "last_status", length = 16)
    private String lastStatus;

    @Column(name = "last_message", length = 300)
    private String lastMessage;

    @Column(name = "run_count")
    private long runCount = 0;
}
