package com.opengym.model;

import jakarta.persistence.*;

@Entity
@Table(name = "set_sides")
public class SetSide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "set_id", nullable = false)
    private Long setId;

    @Column(nullable = false)
    private String side;

    private Double weight;

    private Integer reps;

    @Column(nullable = false)
    private Integer done;

    private Double rir;

    private Double rpe;

    @Column(name = "set_type", nullable = false)
    private String setType;

    public SetSide() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSetId() { return setId; }
    public void setSetId(Long setId) { this.setId = setId; }

    public String getSide() { return side; }
    public void setSide(String side) { this.side = side; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public Integer getReps() { return reps; }
    public void setReps(Integer reps) { this.reps = reps; }

    public Integer getDone() { return done; }
    public void setDone(Integer done) { this.done = done; }

    public Double getRir() { return rir; }
    public void setRir(Double rir) { this.rir = rir; }

    public Double getRpe() { return rpe; }
    public void setRpe(Double rpe) { this.rpe = rpe; }

    public String getSetType() { return setType; }
    public void setSetType(String setType) { this.setType = setType; }
}
