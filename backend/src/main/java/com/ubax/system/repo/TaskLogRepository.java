package com.ubax.system.repo;

import com.ubax.system.domain.TaskLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskLogRepository extends JpaRepository<TaskLog, Long> {
    List<TaskLog> findAllByOrderByStartAtDesc(Pageable pageable);
    List<TaskLog> findByTaskCodeOrderByStartAtDesc(String taskCode, Pageable pageable);
}
