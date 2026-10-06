package com.opengym.repository;

import com.opengym.model.WorkoutSet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkoutSetRepository extends JpaRepository<WorkoutSet, Long> {
    List<WorkoutSet> findByEntryId(Long entryId);
    List<WorkoutSet> findByEntryIdOrderBySetOrder(Long entryId);
}
