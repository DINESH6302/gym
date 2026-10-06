package com.opengym.repository;

import com.opengym.model.Routine;
import com.opengym.model.UserEntityId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoutineRepository extends JpaRepository<Routine, UserEntityId> {
    List<Routine> findByUserId(String userId);
    List<Routine> findByUserIdOrderBySortOrder(String userId);
    Optional<Routine> findByUserIdAndId(String userId, String id);
}
