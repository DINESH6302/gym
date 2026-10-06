package com.opengym.model;

import jakarta.persistence.*;

@Entity
@Table(name = "workout_prs")
public class WorkoutPr {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "workout_id", nullable = false)
    private String workoutId;

    @Column(name = "exercise_id", nullable = false)
    private String exerciseId;

    @Column(name = "pr_type")
    private String prType;

    private Double value;

    public WorkoutPr() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getWorkoutId() { return workoutId; }
    public void setWorkoutId(String workoutId) { this.workoutId = workoutId; }

    public String getExerciseId() { return exerciseId; }
    public void setExerciseId(String exerciseId) { this.exerciseId = exerciseId; }

    public String getPrType() { return prType; }
    public void setPrType(String prType) { this.prType = prType; }

    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }
}
