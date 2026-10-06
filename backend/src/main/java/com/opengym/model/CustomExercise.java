package com.opengym.model;

import jakarta.persistence.*;

@Entity
@Table(name = "custom_exercises")
@IdClass(UserEntityId.class)
public class CustomExercise {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(name = "body_part")
    private String bodyPart;

    private String equipment;

    private String target;

    private String description;

    @Column(name = "created_at", nullable = false)
    private String createdAt;

    public CustomExercise() {}

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBodyPart() { return bodyPart; }
    public void setBodyPart(String bodyPart) { this.bodyPart = bodyPart; }

    public String getEquipment() { return equipment; }
    public void setEquipment(String equipment) { this.equipment = equipment; }

    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
