package com.opengym.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_state")
public class UserState {
    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "state_json", nullable = false, columnDefinition = "TEXT")
    private String stateJson = "{}";

    @Column(nullable = false)
    private int rev = 0;

    @Column(name = "updated_at", nullable = false)
    private String updatedAt;

    public UserState() {}

    public UserState(String userId) {
        this.userId = userId;
        this.updatedAt = java.time.Instant.now().toString();
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getStateJson() { return stateJson; }
    public void setStateJson(String stateJson) { this.stateJson = stateJson; }

    public int getRev() { return rev; }
    public void setRev(int rev) { this.rev = rev; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
