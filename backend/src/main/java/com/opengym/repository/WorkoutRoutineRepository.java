package com.opengym.repository;

import com.opengym.model.WorkoutRoutine;
import com.opengym.model.WorkoutRoutineId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkoutRoutineRepository extends JpaRepository<WorkoutRoutine, WorkoutRoutineId> {
    List<WorkoutRoutine> findByUserId(String userId);
    List<WorkoutRoutine> findByUserIdAndWorkoutId(String userId, String workoutId);
    List<WorkoutRoutine> findByUserIdAndWorkoutIdOrderBySortOrder(String userId, String workoutId);
}
