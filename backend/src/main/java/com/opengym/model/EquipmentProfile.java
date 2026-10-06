package com.opengym.model;

import jakarta.persistence.*;

@Entity
@Table(name = "equipment_profiles")
@IdClass(UserEntityId.class)
public class EquipmentProfile {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    public EquipmentProfile() {}

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
