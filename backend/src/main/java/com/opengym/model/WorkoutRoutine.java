package com.opengym.model;

import jakarta.persistence.*;

@Entity
@Table(name = "workout_routines")
@IdClass(WorkoutRoutineId.class)
public class WorkoutRoutine {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Id
    @Column(name = "workout_id")
    private String workoutId;

    @Id
    @Column(name = "routine_id")
    private String routineId;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    public WorkoutRoutine() {}

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getWorkoutId() { return workoutId; }
    public void setWorkoutId(String workoutId) { this.workoutId = workoutId; }

    public String getRoutineId() { return routineId; }
    public void setRoutineId(String routineId) { this.routineId = routineId; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
