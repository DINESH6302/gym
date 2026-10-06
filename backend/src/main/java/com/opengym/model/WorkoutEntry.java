package com.opengym.model;

import jakarta.persistence.*;

@Entity
@Table(name = "workout_entries")
public class WorkoutEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "workout_id", nullable = false)
    private String workoutId;

    @Column(name = "exercise_id", nullable = false)
    private String exerciseId;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "top_weight")
    private Double topWeight;

    @Column(name = "routine_id")
    private String routineId;

    @Column(name = "no_progression", nullable = false)
    private Integer noProgression;

    private String note;

    @Column(name = "note_pin", nullable = false)
    private Integer notePin;

    @Column(name = "superset_group")
    private String supersetGroup;

    @Column(name = "target_json")
    private String targetJson;

    @Column(name = "muscle_snapshot_json")
    private String muscleSnapshotJson;

    public WorkoutEntry() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getWorkoutId() { return workoutId; }
    public void setWorkoutId(String workoutId) { this.workoutId = workoutId; }

    public String getExerciseId() { return exerciseId; }
    public void setExerciseId(String exerciseId) { this.exerciseId = exerciseId; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public Double getTopWeight() { return topWeight; }
    public void setTopWeight(Double topWeight) { this.topWeight = topWeight; }

    public String getRoutineId() { return routineId; }
    public void setRoutineId(String routineId) { this.routineId = routineId; }

    public Integer getNoProgression() { return noProgression; }
    public void setNoProgression(Integer noProgression) { this.noProgression = noProgression; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Integer getNotePin() { return notePin; }
    public void setNotePin(Integer notePin) { this.notePin = notePin; }

    public String getSupersetGroup() { return supersetGroup; }
    public void setSupersetGroup(String supersetGroup) { this.supersetGroup = supersetGroup; }

    public String getTargetJson() { return targetJson; }
    public void setTargetJson(String targetJson) { this.targetJson = targetJson; }

    public String getMuscleSnapshotJson() { return muscleSnapshotJson; }
    public void setMuscleSnapshotJson(String muscleSnapshotJson) { this.muscleSnapshotJson = muscleSnapshotJson; }
}
