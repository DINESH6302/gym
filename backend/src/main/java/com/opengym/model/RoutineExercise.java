package com.opengym.model;

import jakarta.persistence.*;

@Entity
@Table(name = "routine_exercises")
public class RoutineExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "routine_id", nullable = false)
    private String routineId;

    @Column(name = "exercise_id", nullable = false)
    private String exerciseId;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "superset_group")
    private String supersetGroup;

    private Integer sets;

    private Integer reps;

    @Column(name = "reps_min")
    private Integer repsMin;

    @Column(name = "reps_max")
    private Integer repsMax;

    private Double weight;

    private String mode;

    private Integer sec;

    @Column(name = "min_duration")
    private Integer minDuration;

    private Double speed;

    @Column(nullable = false)
    private Integer bodyweight;

    @Column(name = "per_side", nullable = false)
    private Integer perSide;

    private Integer assisted;

    @Column(name = "rest_sec")
    private Integer restSec;

    @Column(name = "warmup_rest_sec")
    private Integer warmupRestSec;

    @Column(name = "warmup_sets")
    private Integer warmupSets;

    private String progression;

    private Double increment;

    @Column(name = "deload_factor")
    private Double deloadFactor;

    private String note;

    public RoutineExercise() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getRoutineId() { return routineId; }
    public void setRoutineId(String routineId) { this.routineId = routineId; }

    public String getExerciseId() { return exerciseId; }
    public void setExerciseId(String exerciseId) { this.exerciseId = exerciseId; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public String getSupersetGroup() { return supersetGroup; }
    public void setSupersetGroup(String supersetGroup) { this.supersetGroup = supersetGroup; }

    public Integer getSets() { return sets; }
    public void setSets(Integer sets) { this.sets = sets; }

    public Integer getReps() { return reps; }
    public void setReps(Integer reps) { this.reps = reps; }

    public Integer getRepsMin() { return repsMin; }
    public void setRepsMin(Integer repsMin) { this.repsMin = repsMin; }

    public Integer getRepsMax() { return repsMax; }
    public void setRepsMax(Integer repsMax) { this.repsMax = repsMax; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public Integer getSec() { return sec; }
    public void setSec(Integer sec) { this.sec = sec; }

    public Integer getMinDuration() { return minDuration; }
    public void setMinDuration(Integer minDuration) { this.minDuration = minDuration; }

    public Double getSpeed() { return speed; }
    public void setSpeed(Double speed) { this.speed = speed; }

    public Integer getBodyweight() { return bodyweight; }
    public void setBodyweight(Integer bodyweight) { this.bodyweight = bodyweight; }

    public Integer getPerSide() { return perSide; }
    public void setPerSide(Integer perSide) { this.perSide = perSide; }

    public Integer getAssisted() { return assisted; }
    public void setAssisted(Integer assisted) { this.assisted = assisted; }

    public Integer getRestSec() { return restSec; }
    public void setRestSec(Integer restSec) { this.restSec = restSec; }

    public Integer getWarmupRestSec() { return warmupRestSec; }
    public void setWarmupRestSec(Integer warmupRestSec) { this.warmupRestSec = warmupRestSec; }

    public Integer getWarmupSets() { return warmupSets; }
    public void setWarmupSets(Integer warmupSets) { this.warmupSets = warmupSets; }

    public String getProgression() { return progression; }
    public void setProgression(String progression) { this.progression = progression; }

    public Double getIncrement() { return increment; }
    public void setIncrement(Double increment) { this.increment = increment; }

    public Double getDeloadFactor() { return deloadFactor; }
    public void setDeloadFactor(Double deloadFactor) { this.deloadFactor = deloadFactor; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
