package com.opengym.repository;

import com.opengym.model.RoutineExerciseIntensifier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoutineExerciseIntensifierRepository extends JpaRepository<RoutineExerciseIntensifier, Long> {
    Optional<RoutineExerciseIntensifier> findByRoutineExerciseId(Long routineExerciseId);
}
