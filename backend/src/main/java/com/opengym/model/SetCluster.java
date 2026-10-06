package com.opengym.model;

import jakarta.persistence.*;

@Entity
@Table(name = "set_clusters")
public class SetCluster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "set_id")
    private Long setId;

    @Column(name = "side_id")
    private Long sideId;

    @Column(name = "cluster_order", nullable = false)
    private Integer clusterOrder;

    @Column(nullable = false)
    private Integer reps;

    @Column(name = "rest_sec", nullable = false)
    private Integer restSec;

    public SetCluster() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSetId() { return setId; }
    public void setSetId(Long setId) { this.setId = setId; }

    public Long getSideId() { return sideId; }
    public void setSideId(Long sideId) { this.sideId = sideId; }

    public Integer getClusterOrder() { return clusterOrder; }
    public void setClusterOrder(Integer clusterOrder) { this.clusterOrder = clusterOrder; }

    public Integer getReps() { return reps; }
    public void setReps(Integer reps) { this.reps = reps; }

    public Integer getRestSec() { return restSec; }
    public void setRestSec(Integer restSec) { this.restSec = restSec; }
}
