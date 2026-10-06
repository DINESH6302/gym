package com.opengym.repository;

import com.opengym.model.CustomExercise;
import com.opengym.model.UserEntityId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomExerciseRepository extends JpaRepository<CustomExercise, UserEntityId> {
    List<CustomExercise> findByUserId(String userId);
    Optional<CustomExercise> findByUserIdAndId(String userId, String id);
}
