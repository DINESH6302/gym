package com.opengym.model;

import jakarta.persistence.*;

@Entity
@Table(name = "week_schedule")
@IdClass(WeekScheduleId.class)
public class WeekSchedule {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Id
    private Integer weekday;

    @Id
    @Column(name = "routine_id")
    private String routineId;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    public WeekSchedule() {}

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Integer getWeekday() { return weekday; }
    public void setWeekday(Integer weekday) { this.weekday = weekday; }

    public String getRoutineId() { return routineId; }
    public void setRoutineId(String routineId) { this.routineId = routineId; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
