package com.opengym.repository;

import com.opengym.model.BarWeight;
import com.opengym.model.UserExerciseId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BarWeightRepository extends JpaRepository<BarWeight, UserExerciseId> {
    List<BarWeight> findByUserId(String userId);
    Optional<BarWeight> findByUserIdAndExerciseId(String userId, String exerciseId);
}
