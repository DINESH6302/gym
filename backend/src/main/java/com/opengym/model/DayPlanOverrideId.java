package com.opengym.model;

import java.io.Serializable;
import java.util.Objects;

public class DayPlanOverrideId implements Serializable {
    private String userId;
    private String dateIso;

    public DayPlanOverrideId() {}

    public DayPlanOverrideId(String userId, String dateIso) {
        this.userId = userId;
        this.dateIso = dateIso;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getDateIso() { return dateIso; }
    public void setDateIso(String dateIso) { this.dateIso = dateIso; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DayPlanOverrideId)) return false;
        DayPlanOverrideId that = (DayPlanOverrideId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(dateIso, that.dateIso);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, dateIso);
    }
}
