package com.opengym.model;

import jakarta.persistence.*;

@Entity
@Table(name = "routine_exercise_intensifiers")
public class RoutineExerciseIntensifier {

    @Id
    @Column(name = "routine_exercise_id")
    private Long routineExerciseId;

    @Column(nullable = false)
    private String type;

    @Column(name = "drop_count")
    private Integer dropCount;

    @Column(name = "drop_pct")
    private Integer dropPct;

    @Column(name = "total_reps")
    private Integer totalReps;

    @Column(name = "rest_sec")
    private Integer restSec;

    public RoutineExerciseIntensifier() {}

    public Long getRoutineExerciseId() { return routineExerciseId; }
    public void setRoutineExerciseId(Long routineExerciseId) { this.routineExerciseId = routineExerciseId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getDropCount() { return dropCount; }
    public void setDropCount(Integer dropCount) { this.dropCount = dropCount; }

    public Integer getDropPct() { return dropPct; }
    public void setDropPct(Integer dropPct) { this.dropPct = dropPct; }

    public Integer getTotalReps() { return totalReps; }
    public void setTotalReps(Integer totalReps) { this.totalReps = totalReps; }

    public Integer getRestSec() { return restSec; }
    public void setRestSec(Integer restSec) { this.restSec = restSec; }
}
