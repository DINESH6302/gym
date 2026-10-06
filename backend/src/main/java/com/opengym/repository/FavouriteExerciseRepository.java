package com.opengym.repository;

import com.opengym.model.FavouriteExercise;
import com.opengym.model.UserExerciseId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavouriteExerciseRepository extends JpaRepository<FavouriteExercise, UserExerciseId> {
    List<FavouriteExercise> findByUserId(String userId);
    List<FavouriteExercise> findByUserIdOrderBySortOrder(String userId);
}
