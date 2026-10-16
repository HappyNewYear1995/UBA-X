package com.ubax.system.repo;

import com.ubax.system.domain.ScheduledTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScheduledTaskRepository extends JpaRepository<ScheduledTask, Long> {
    Optional<ScheduledTask> findByCode(String code);
    List<ScheduledTask> findByEnabledTrue();
    List<ScheduledTask> findAllByOrderByIdAsc();
}
