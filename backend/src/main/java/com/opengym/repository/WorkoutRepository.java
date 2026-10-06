package com.opengym.repository;

import com.opengym.model.Workout;
import com.opengym.model.UserEntityId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkoutRepository extends JpaRepository<Workout, UserEntityId> {
    List<Workout> findByUserId(String userId);
    List<Workout> findByUserIdOrderByStartTimeDesc(String userId);
    List<Workout> findByUserIdAndDateIso(String userId, String dateIso);
    Optional<Workout> findByUserIdAndId(String userId, String id);
}
