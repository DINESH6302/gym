package com.opengym.model;

import java.io.Serializable;
import java.util.Objects;

public class EquipmentProfileItemId implements Serializable {
    private String userId;
    private String profileId;
    private String equipment;

    public EquipmentProfileItemId() {}

    public EquipmentProfileItemId(String userId, String profileId, String equipment) {
        this.userId = userId;
        this.profileId = profileId;
        this.equipment = equipment;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getProfileId() { return profileId; }
    public void setProfileId(String profileId) { this.profileId = profileId; }

    public String getEquipment() { return equipment; }
    public void setEquipment(String equipment) { this.equipment = equipment; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EquipmentProfileItemId)) return false;
        EquipmentProfileItemId that = (EquipmentProfileItemId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(profileId, that.profileId) && Objects.equals(equipment, that.equipment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, profileId, equipment);
    }
}
