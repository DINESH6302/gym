package com.opengym.model;

import jakarta.persistence.*;

@Entity
@Table(name = "favourite_exercises")
@IdClass(UserExerciseId.class)
public class FavouriteExercise {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Id
    @Column(name = "exercise_id")
    private String exerciseId;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    public FavouriteExercise() {}

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getExerciseId() { return exerciseId; }
    public void setExerciseId(String exerciseId) { this.exerciseId = exerciseId; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
