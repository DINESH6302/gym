package com.opengym.model;

import jakarta.persistence.*;

@Entity
@Table(name = "exercise_notes")
@IdClass(UserExerciseId.class)
public class ExerciseNote {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Id
    @Column(name = "exercise_id")
    private String exerciseId;

    @Column(nullable = false)
    private String note;

    public ExerciseNote() {}

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getExerciseId() { return exerciseId; }
    public void setExerciseId(String exerciseId) { this.exerciseId = exerciseId; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
