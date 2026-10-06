package com.opengym.repository;

import com.opengym.model.ExerciseWeight;
import com.opengym.model.UserExerciseId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExerciseWeightRepository extends JpaRepository<ExerciseWeight, UserExerciseId> {
    List<ExerciseWeight> findByUserId(String userId);
    Optional<ExerciseWeight> findByUserIdAndExerciseId(String userId, String exerciseId);
}
