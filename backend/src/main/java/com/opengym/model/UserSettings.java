package com.opengym.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_settings")
public class UserSettings {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(nullable = false)
    private String unit;

    @Column(nullable = false)
    private Integer sound;

    @Column(name = "sound_on_silent", nullable = false)
    private Integer soundOnSilent;

    @Column(name = "keep_awake", nullable = false)
    private Integer keepAwake;

    @Column(nullable = false)
    private String lang;

    @Column(nullable = false)
    private String theme;

    @Column(nullable = false)
    private String accent;

    @Column(nullable = false)
    private String body;

    @Column(name = "target_weight")
    private Double targetWeight;

    @Column(name = "gif_size", nullable = false)
    private String gifSize;

    @Column(name = "workout_view", nullable = false)
    private String workoutView;

    private String effort;

    @Column(name = "reminder_on", nullable = false)
    private Integer reminderOn;

    @Column(name = "reminder_time")
    private String reminderTime;

    @Column(name = "reminder_tz")
    private String reminderTz;

    @Column(name = "auto_backup", nullable = false)
    private Integer autoBackup;

    @Column(name = "equip_filter_on", nullable = false)
    private Integer equipFilterOn;

    @Column(name = "active_equip_id")
    private String activeEquipId;

    @Column(name = "weigh_in", nullable = false)
    private Integer weighIn;

    @Column(name = "week_start", nullable = false)
    private Integer weekStart;

    @Column(name = "weight_decimals", nullable = false)
    private Integer weightDecimals;

    @Column(name = "wc_steppers", nullable = false)
    private Integer wcSteppers;

    @Column(name = "wc_set_shortcuts", nullable = false)
    private Integer wcSetShortcuts;

    @Column(name = "wc_pair_buttons", nullable = false)
    private Integer wcPairButtons;

    @Column(name = "wc_exercise_buttons", nullable = false)
    private Integer wcExerciseButtons;

    @Column(name = "updated_at", nullable = false)
    private String updatedAt;

    @Column(nullable = false)
    private Integer rev;

    public UserSettings() {}

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public Integer getSound() { return sound; }
    public void setSound(Integer sound) { this.sound = sound; }

    public Integer getSoundOnSilent() { return soundOnSilent; }
    public void setSoundOnSilent(Integer soundOnSilent) { this.soundOnSilent = soundOnSilent; }

    public Integer getKeepAwake() { return keepAwake; }
    public void setKeepAwake(Integer keepAwake) { this.keepAwake = keepAwake; }

    public String getLang() { return lang; }
    public void setLang(String lang) { this.lang = lang; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public String getAccent() { return accent; }
    public void setAccent(String accent) { this.accent = accent; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public Double getTargetWeight() { return targetWeight; }
    public void setTargetWeight(Double targetWeight) { this.targetWeight = targetWeight; }

    public String getGifSize() { return gifSize; }
    public void setGifSize(String gifSize) { this.gifSize = gifSize; }

    public String getWorkoutView() { return workoutView; }
    public void setWorkoutView(String workoutView) { this.workoutView = workoutView; }

    public String getEffort() { return effort; }
    public void setEffort(String effort) { this.effort = effort; }

    public Integer getReminderOn() { return reminderOn; }
    public void setReminderOn(Integer reminderOn) { this.reminderOn = reminderOn; }

    public String getReminderTime() { return reminderTime; }
    public void setReminderTime(String reminderTime) { this.reminderTime = reminderTime; }

    public String getReminderTz() { return reminderTz; }
    public void setReminderTz(String reminderTz) { this.reminderTz = reminderTz; }

    public Integer getAutoBackup() { return autoBackup; }
    public void setAutoBackup(Integer autoBackup) { this.autoBackup = autoBackup; }

    public Integer getEquipFilterOn() { return equipFilterOn; }
    public void setEquipFilterOn(Integer equipFilterOn) { this.equipFilterOn = equipFilterOn; }

    public String getActiveEquipId() { return activeEquipId; }
    public void setActiveEquipId(String activeEquipId) { this.activeEquipId = activeEquipId; }

    public Integer getWeighIn() { return weighIn; }
    public void setWeighIn(Integer weighIn) { this.weighIn = weighIn; }

    public Integer getWeekStart() { return weekStart; }
    public void setWeekStart(Integer weekStart) { this.weekStart = weekStart; }

    public Integer getWeightDecimals() { return weightDecimals; }
    public void setWeightDecimals(Integer weightDecimals) { this.weightDecimals = weightDecimals; }

    public Integer getWcSteppers() { return wcSteppers; }
    public void setWcSteppers(Integer wcSteppers) { this.wcSteppers = wcSteppers; }

    public Integer getWcSetShortcuts() { return wcSetShortcuts; }
    public void setWcSetShortcuts(Integer wcSetShortcuts) { this.wcSetShortcuts = wcSetShortcuts; }

    public Integer getWcPairButtons() { return wcPairButtons; }
    public void setWcPairButtons(Integer wcPairButtons) { this.wcPairButtons = wcPairButtons; }

    public Integer getWcExerciseButtons() { return wcExerciseButtons; }
    public void setWcExerciseButtons(Integer wcExerciseButtons) { this.wcExerciseButtons = wcExerciseButtons; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public Integer getRev() { return rev; }
    public void setRev(Integer rev) { this.rev = rev; }
}
