package com.opengym.model;

import jakarta.persistence.*;

@Entity
@Table(name = "workouts")
@IdClass(UserEntityId.class)
public class Workout {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Id
    private String id;

    @Column(name = "date_iso", nullable = false)
    private String dateIso;

    @Column(name = "start_time")
    private Long startTime;

    @Column(name = "end_time")
    private Long endTime;

    private String name;

    private Double bodyweight;

    private String note;

    @Column(name = "exclude_from_progression", nullable = false)
    private Integer excludeFromProgression;

    @Column(name = "created_at", nullable = false)
    private String createdAt;

    public Workout() {}

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDateIso() { return dateIso; }
    public void setDateIso(String dateIso) { this.dateIso = dateIso; }

    public Long getStartTime() { return startTime; }
    public void setStartTime(Long startTime) { this.startTime = startTime; }

    public Long getEndTime() { return endTime; }
    public void setEndTime(Long endTime) { this.endTime = endTime; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getBodyweight() { return bodyweight; }
    public void setBodyweight(Double bodyweight) { this.bodyweight = bodyweight; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Integer getExcludeFromProgression() { return excludeFromProgression; }
    public void setExcludeFromProgression(Integer excludeFromProgression) { this.excludeFromProgression = excludeFromProgression; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
