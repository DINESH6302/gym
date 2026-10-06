package com.opengym.model;

import jakarta.persistence.*;

@Entity
@Table(name = "exercise_weights")
@IdClass(UserExerciseId.class)
public class ExerciseWeight {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Id
    @Column(name = "exercise_id")
    private String exerciseId;

    @Column(nullable = false)
    private Double weight;

    public ExerciseWeight() {}

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getExerciseId() { return exerciseId; }
    public void setExerciseId(String exerciseId) { this.exerciseId = exerciseId; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }
}
