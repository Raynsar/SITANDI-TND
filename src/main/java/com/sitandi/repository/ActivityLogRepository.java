package com.sitandi.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sitandi.model.ActivityLog;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findAllByOrderByCreatedAtDesc();

    List<ActivityLog> findByAksiOrderByCreatedAtDesc(String aksi);

    List<ActivityLog> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);

    List<ActivityLog> findByAksiAndCreatedAtBetweenOrderByCreatedAtDesc(String aksi, LocalDateTime start, LocalDateTime end);
}
