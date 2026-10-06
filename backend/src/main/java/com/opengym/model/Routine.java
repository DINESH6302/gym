package com.opengym.model;

import jakarta.persistence.*;

@Entity
@Table(name = "routines")
@IdClass(UserEntityId.class)
public class Routine {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    private String emoji;

    @Column(name = "progression_policy")
    private String progressionPolicy;

    @Column(name = "exclude_from_progression", nullable = false)
    private Integer excludeFromProgression;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "created_at", nullable = false)
    private String createdAt;

    public Routine() {}

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }

    public String getProgressionPolicy() { return progressionPolicy; }
    public void setProgressionPolicy(String progressionPolicy) { this.progressionPolicy = progressionPolicy; }

    public Integer getExcludeFromProgression() { return excludeFromProgression; }
    public void setExcludeFromProgression(Integer excludeFromProgression) { this.excludeFromProgression = excludeFromProgression; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
