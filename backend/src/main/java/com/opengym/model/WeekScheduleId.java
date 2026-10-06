package com.opengym.model;

import java.io.Serializable;
import java.util.Objects;

public class WeekScheduleId implements Serializable {
    private String userId;
    private Integer weekday;
    private String routineId;

    public WeekScheduleId() {}

    public WeekScheduleId(String userId, Integer weekday, String routineId) {
        this.userId = userId;
        this.weekday = weekday;
        this.routineId = routineId;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Integer getWeekday() { return weekday; }
    public void setWeekday(Integer weekday) { this.weekday = weekday; }

    public String getRoutineId() { return routineId; }
    public void setRoutineId(String routineId) { this.routineId = routineId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WeekScheduleId)) return false;
        WeekScheduleId that = (WeekScheduleId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(weekday, that.weekday) && Objects.equals(routineId, that.routineId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, weekday, routineId);
    }
}
