package com.foodsharing.repository;

import com.foodsharing.entity.SystemLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;

public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {
    Page<SystemLog> findByModule(String module, Pageable pageable);
    Page<SystemLog> findByLevel(String level, Pageable pageable);
    Page<SystemLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    Page<SystemLog> findByModuleAndLevel(String module, String level, Pageable pageable);
}

