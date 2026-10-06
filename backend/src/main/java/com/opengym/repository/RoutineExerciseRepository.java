package com.opengym.repository;

import com.opengym.model.RoutineExercise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoutineExerciseRepository extends JpaRepository<RoutineExercise, Long> {
    List<RoutineExercise> findByUserIdAndRoutineId(String userId, String routineId);
    List<RoutineExercise> findByUserIdAndRoutineIdOrderBySortOrder(String userId, String routineId);
    List<RoutineExercise> findByUserId(String userId);
}
