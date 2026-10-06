package com.opengym.model;

import jakarta.persistence.*;

@Entity
@Table(name = "workout_sets")
public class WorkoutSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entry_id", nullable = false)
    private Long entryId;

    @Column(name = "set_order", nullable = false)
    private Integer setOrder;

    private Double weight;

    private Integer reps;

    @Column(nullable = false)
    private Integer done;

    @Column(nullable = false)
    private String phase;

    private String mode;

    private Integer seconds;

    private Double minutes;

    private Double speed;

    private Double rir;

    private Double rpe;

    @Column(name = "set_type", nullable = false)
    private String setType;

    @Column(name = "is_per_side", nullable = false)
    private Integer isPerSide;

    public WorkoutSet() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEntryId() { return entryId; }
    public void setEntryId(Long entryId) { this.entryId = entryId; }

    public Integer getSetOrder() { return setOrder; }
    public void setSetOrder(Integer setOrder) { this.setOrder = setOrder; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public Integer getReps() { return reps; }
    public void setReps(Integer reps) { this.reps = reps; }

    public Integer getDone() { return done; }
    public void setDone(Integer done) { this.done = done; }

    public String getPhase() { return phase; }
    public void setPhase(String phase) { this.phase = phase; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public Integer getSeconds() { return seconds; }
    public void setSeconds(Integer seconds) { this.seconds = seconds; }

    public Double getMinutes() { return minutes; }
    public void setMinutes(Double minutes) { this.minutes = minutes; }

    public Double getSpeed() { return speed; }
    public void setSpeed(Double speed) { this.speed = speed; }

    public Double getRir() { return rir; }
    public void setRir(Double rir) { this.rir = rir; }

    public Double getRpe() { return rpe; }
    public void setRpe(Double rpe) { this.rpe = rpe; }

    public String getSetType() { return setType; }
    public void setSetType(String setType) { this.setType = setType; }

    public Integer getIsPerSide() { return isPerSide; }
    public void setIsPerSide(Integer isPerSide) { this.isPerSide = isPerSide; }
}
