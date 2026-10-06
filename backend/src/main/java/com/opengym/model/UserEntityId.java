package com.opengym.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite PK for entities keyed by (user_id, id):
 * routines, workouts, equipment_profiles, custom_exercises.
 */
public class UserEntityId implements Serializable {
    private String userId;
    private String id;

    public UserEntityId() {}

    public UserEntityId(String userId, String id) {
        this.userId = userId;
        this.id = id;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserEntityId)) return false;
        UserEntityId that = (UserEntityId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, id);
    }
}
