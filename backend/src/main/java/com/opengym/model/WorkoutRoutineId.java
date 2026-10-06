package com.opengym.model;

import java.io.Serializable;
import java.util.Objects;

public class WorkoutRoutineId implements Serializable {
    private String userId;
    private String workoutId;
    private String routineId;

    public WorkoutRoutineId() {}

    public WorkoutRoutineId(String userId, String workoutId, String routineId) {
        this.userId = userId;
        this.workoutId = workoutId;
        this.routineId = routineId;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getWorkoutId() { return workoutId; }
    public void setWorkoutId(String workoutId) { this.workoutId = workoutId; }

    public String getRoutineId() { return routineId; }
    public void setRoutineId(String routineId) { this.routineId = routineId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkoutRoutineId)) return false;
        WorkoutRoutineId that = (WorkoutRoutineId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(workoutId, that.workoutId) && Objects.equals(routineId, that.routineId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, workoutId, routineId);
    }
}
