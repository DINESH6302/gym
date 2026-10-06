package com.opengym.model;

import jakarta.persistence.*;

@Entity
@Table(name = "set_drops")
public class SetDrop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "set_id")
    private Long setId;

    @Column(name = "side_id")
    private Long sideId;

    @Column(name = "drop_order", nullable = false)
    private Integer dropOrder;

    @Column(nullable = false)
    private Double weight;

    @Column(nullable = false)
    private Integer reps;

    public SetDrop() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSetId() { return setId; }
    public void setSetId(Long setId) { this.setId = setId; }

    public Long getSideId() { return sideId; }
    public void setSideId(Long sideId) { this.sideId = sideId; }

    public Integer getDropOrder() { return dropOrder; }
    public void setDropOrder(Integer dropOrder) { this.dropOrder = dropOrder; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public Integer getReps() { return reps; }
    public void setReps(Integer reps) { this.reps = reps; }
}
