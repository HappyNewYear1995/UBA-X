package com.ubax.system.repo;

import com.ubax.system.domain.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    List<Notice> findAllByOrderByTopDescPublishedAtDescIdDesc();
    List<Notice> findByStatusOrderByTopDescPublishedAtDescIdDesc(String status);
    long countByStatus(String status);
}
