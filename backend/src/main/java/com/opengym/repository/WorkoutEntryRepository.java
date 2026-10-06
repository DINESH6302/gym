package com.opengym.repository;

import com.opengym.model.WorkoutEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkoutEntryRepository extends JpaRepository<WorkoutEntry, Long> {
    List<WorkoutEntry> findByUserId(String userId);
    List<WorkoutEntry> findByUserIdAndWorkoutId(String userId, String workoutId);
    List<WorkoutEntry> findByUserIdAndWorkoutIdOrderBySortOrder(String userId, String workoutId);
    List<WorkoutEntry> findByUserIdAndExerciseId(String userId, String exerciseId);
}
