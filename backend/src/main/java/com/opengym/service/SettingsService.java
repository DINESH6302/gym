package com.opengym.service;

import com.opengym.model.UserSettings;
import com.opengym.repository.UserSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Service
public class SettingsService {

    private final UserSettingsRepository settingsRepo;

    public SettingsService(UserSettingsRepository settingsRepo) {
        this.settingsRepo = settingsRepo;
    }

    public Optional<UserSettings> getSettings(String userId) {
        return settingsRepo.findByUserId(userId);
    }

    /** Create default settings for a new user. */
    @Transactional
    public UserSettings createDefaults(String userId) {
        UserSettings s = new UserSettings();
        s.setUserId(userId);
        s.setUnit("kg");
        s.setSound(1);
        s.setSoundOnSilent(0);
        s.setKeepAwake(1);
        s.setLang("en");
        s.setTheme("system");
        s.setAccent("blue");
        s.setBody("male");
        s.setGifSize("small");
        s.setWorkoutView("list");
        s.setReminderOn(0);
        s.setAutoBackup(0);
        s.setEquipFilterOn(0);
        s.setWeighIn(0);
        s.setWeekStart(1);
        s.setWeightDecimals(1);
        s.setWcSteppers(1);
        s.setWcSetShortcuts(1);
        s.setWcPairButtons(0);
        s.setWcExerciseButtons(1);
        s.setUpdatedAt(Instant.now().toString());
        s.setRev(1);
        return settingsRepo.save(s);
    }

    /** Partial-update: only overwrite fields that are present in the map. */
    @Transactional
    public UserSettings updateSettings(String userId, Map<String, Object> fields) {
        UserSettings s = settingsRepo.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Settings not found"));

        if (fields.containsKey("unit"))              s.setUnit((String) fields.get("unit"));
        if (fields.containsKey("sound"))             s.setSound(toInt(fields.get("sound")));
        if (fields.containsKey("soundOnSilent"))     s.setSoundOnSilent(toInt(fields.get("soundOnSilent")));
        if (fields.containsKey("keepAwake"))          s.setKeepAwake(toInt(fields.get("keepAwake")));
        if (fields.containsKey("lang"))              s.setLang((String) fields.get("lang"));
        if (fields.containsKey("theme"))             s.setTheme((String) fields.get("theme"));
        if (fields.containsKey("accent"))            s.setAccent((String) fields.get("accent"));
        if (fields.containsKey("body"))              s.setBody((String) fields.get("body"));
        if (fields.containsKey("targetWeight"))      s.setTargetWeight(toDouble(fields.get("targetWeight")));
        if (fields.containsKey("gifSize"))           s.setGifSize((String) fields.get("gifSize"));
        if (fields.containsKey("workoutView"))       s.setWorkoutView((String) fields.get("workoutView"));
        if (fields.containsKey("effort"))            s.setEffort((String) fields.get("effort"));
        if (fields.containsKey("reminderOn"))        s.setReminderOn(toInt(fields.get("reminderOn")));
        if (fields.containsKey("reminderTime"))      s.setReminderTime((String) fields.get("reminderTime"));
        if (fields.containsKey("reminderTz"))        s.setReminderTz((String) fields.get("reminderTz"));
        if (fields.containsKey("autoBackup"))        s.setAutoBackup(toInt(fields.get("autoBackup")));
        if (fields.containsKey("equipFilterOn"))     s.setEquipFilterOn(toInt(fields.get("equipFilterOn")));
        if (fields.containsKey("activeEquipId"))     s.setActiveEquipId((String) fields.get("activeEquipId"));
        if (fields.containsKey("weighIn"))           s.setWeighIn(toInt(fields.get("weighIn")));
        if (fields.containsKey("weekStart"))         s.setWeekStart(toInt(fields.get("weekStart")));
        if (fields.containsKey("weightDecimals"))    s.setWeightDecimals(toInt(fields.get("weightDecimals")));
        if (fields.containsKey("wcSteppers"))        s.setWcSteppers(toInt(fields.get("wcSteppers")));
        if (fields.containsKey("wcSetShortcuts"))    s.setWcSetShortcuts(toInt(fields.get("wcSetShortcuts")));
        if (fields.containsKey("wcPairButtons"))     s.setWcPairButtons(toInt(fields.get("wcPairButtons")));
        if (fields.containsKey("wcExerciseButtons")) s.setWcExerciseButtons(toInt(fields.get("wcExerciseButtons")));

        s.setUpdatedAt(Instant.now().toString());
        s.setRev(s.getRev() + 1);
        return settingsRepo.save(s);
    }

    private Integer toInt(Object v) {
        if (v == null) return null;
        if (v instanceof Boolean) return (Boolean) v ? 1 : 0;
        return ((Number) v).intValue();
    }

    private Double toDouble(Object v) {
        if (v == null) return null;
        if (v instanceof Boolean) return (Boolean) v ? 1.0 : 0.0;
        return ((Number) v).doubleValue();
    }
}
