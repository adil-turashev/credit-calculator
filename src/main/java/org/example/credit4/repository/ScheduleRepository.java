package org.example.credit4.repository;

import org.example.credit4.entity.ScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<ScheduleEntity, Long> {
    List<ScheduleEntity> findByRequest_IdOrderByMonthNumberAsc(Long requestId);
}
