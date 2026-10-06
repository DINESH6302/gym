package com.opengym.repository;

import com.opengym.model.WeekSchedule;
import com.opengym.model.WeekScheduleId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WeekScheduleRepository extends JpaRepository<WeekSchedule, WeekScheduleId> {
    List<WeekSchedule> findByUserId(String userId);
    List<WeekSchedule> findByUserIdAndWeekday(String userId, Integer weekday);
    List<WeekSchedule> findByUserIdOrderByWeekdayAscSortOrderAsc(String userId);
}
