package com.opengym.model;

import jakarta.persistence.*;

@Entity
@Table(name = "equipment_profile_items")
@IdClass(EquipmentProfileItemId.class)
public class EquipmentProfileItem {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Id
    @Column(name = "profile_id")
    private String profileId;

    @Id
    private String equipment;

    public EquipmentProfileItem() {}

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getProfileId() { return profileId; }
    public void setProfileId(String profileId) { this.profileId = profileId; }

    public String getEquipment() { return equipment; }
    public void setEquipment(String equipment) { this.equipment = equipment; }
}
