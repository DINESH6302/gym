package com.opengym.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite PK for entities keyed by (user_id, exercise_id):
 * exercise_weights, exercise_notes, favourite_exercises, bar_weights.
 */
public class UserExerciseId implements Serializable {
    private String userId;
    private String exerciseId;

    public UserExerciseId() {}

    public UserExerciseId(String userId, String exerciseId) {
        this.userId = userId;
        this.exerciseId = exerciseId;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getExerciseId() { return exerciseId; }
    public void setExerciseId(String exerciseId) { this.exerciseId = exerciseId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserExerciseId)) return false;
        UserExerciseId that = (UserExerciseId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(exerciseId, that.exerciseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, exerciseId);
    }
}
