package com.opengym.repository;

import com.opengym.model.ExerciseNote;
import com.opengym.model.UserExerciseId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExerciseNoteRepository extends JpaRepository<ExerciseNote, UserExerciseId> {
    List<ExerciseNote> findByUserId(String userId);
    Optional<ExerciseNote> findByUserIdAndExerciseId(String userId, String exerciseId);
}
