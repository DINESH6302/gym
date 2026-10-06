package com.opengym.model;

import jakarta.persistence.*;

@Entity
@Table(name = "day_plan_overrides")
@IdClass(DayPlanOverrideId.class)
public class DayPlanOverride {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Id
    @Column(name = "date_iso")
    private String dateIso;

    @Column(name = "routine_id")
    private String routineId;

    public DayPlanOverride() {}

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getDateIso() { return dateIso; }
    public void setDateIso(String dateIso) { this.dateIso = dateIso; }

    public String getRoutineId() { return routineId; }
    public void setRoutineId(String routineId) { this.routineId = routineId; }
}
