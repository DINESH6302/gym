package com.opengym.repository;

import com.opengym.model.WorkoutPr;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkoutPrRepository extends JpaRepository<WorkoutPr, Long> {
    List<WorkoutPr> findByUserId(String userId);
    List<WorkoutPr> findByUserIdAndExerciseId(String userId, String exerciseId);
    List<WorkoutPr> findByUserIdAndWorkoutId(String userId, String workoutId);
}
