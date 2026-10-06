package com.opengym.service;

import com.opengym.model.Routine;
import com.opengym.model.RoutineExercise;
import com.opengym.model.RoutineExerciseIntensifier;
import com.opengym.repository.RoutineExerciseIntensifierRepository;
import com.opengym.repository.RoutineExerciseRepository;
import com.opengym.repository.RoutineRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class RoutineService {

    private final RoutineRepository routineRepo;
    private final RoutineExerciseRepository exerciseRepo;
    private final RoutineExerciseIntensifierRepository intensifierRepo;

    public RoutineService(RoutineRepository routineRepo,
                          RoutineExerciseRepository exerciseRepo,
                          RoutineExerciseIntensifierRepository intensifierRepo) {
        this.routineRepo = routineRepo;
        this.exerciseRepo = exerciseRepo;
        this.intensifierRepo = intensifierRepo;
    }

    /**
     * List all routines for a user, with exercises embedded as "ex" arrays.
     * This matches the shape the frontend expects.
     */
    public List<Map<String, Object>> listRoutines(String userId) {
        List<Routine> routines = routineRepo.findByUserIdOrderBySortOrder(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Routine r : routines) {
            result.add(routineToFrontendMap(userId, r));
        }
        return result;
    }

    /** Get a single routine with exercises. */
    public Map<String, Object> getRoutineDetail(String userId, String routineId) {
        Optional<Routine> opt = routineRepo.findByUserIdAndId(userId, routineId);
        if (opt.isEmpty()) return null;
        return routineToFrontendMap(userId, opt.get());
    }

    @Transactional
    public Map<String, Object> createRoutine(String userId, Map<String, Object> body) {
        Routine r = new Routine();
        r.setUserId(userId);
        r.setId(body.containsKey("id") ? (String) body.get("id") : UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        r.setName((String) body.getOrDefault("name", "New Routine"));
        r.setEmoji((String) body.get("emoji"));
        r.setProgressionPolicy((String) body.get("progressionPolicy"));
        r.setExcludeFromProgression(toInt(body.getOrDefault("excludeFromProgression", 0)));
        r.setSortOrder(toInt(body.getOrDefault("sortOrder", 0)));
        r.setCreatedAt(Instant.now().toString());
        routineRepo.save(r);

        // Create exercises from "ex" array if present
        if (body.containsKey("ex") && body.get("ex") instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> exList = (List<Map<String, Object>>) body.get("ex");
            saveExercisesFromFrontend(userId, r.getId(), exList);
        }

        return routineToFrontendMap(userId, r);
    }

    @Transactional
    public Map<String, Object> updateRoutine(String userId, String routineId, Map<String, Object> body) {
        Routine r = routineRepo.findByUserIdAndId(userId, routineId).orElse(null);
        if (r == null) return null;

        if (body.containsKey("name"))                   r.setName((String) body.get("name"));
        if (body.containsKey("emoji"))                  r.setEmoji((String) body.get("emoji"));
        if (body.containsKey("progressionPolicy"))      r.setProgressionPolicy((String) body.get("progressionPolicy"));
        if (body.containsKey("excludeFromProgression")) r.setExcludeFromProgression(toInt(body.get("excludeFromProgression")));
        if (body.containsKey("sortOrder"))              r.setSortOrder(toInt(body.get("sortOrder")));
        routineRepo.save(r);

        // Replace exercises if "ex" array is present
        if (body.containsKey("ex") && body.get("ex") instanceof List) {
            // Delete existing exercises
            List<RoutineExercise> existing = exerciseRepo.findByUserIdAndRoutineId(userId, routineId);
            for (RoutineExercise ex : existing) {
                intensifierRepo.findByRoutineExerciseId(ex.getId()).ifPresent(intensifierRepo::delete);
            }
            exerciseRepo.deleteAll(existing);

            // Recreate from frontend data
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> exList = (List<Map<String, Object>>) body.get("ex");
            saveExercisesFromFrontend(userId, routineId, exList);
        }

        return routineToFrontendMap(userId, r);
    }

    @Transactional
    public boolean deleteRoutine(String userId, String routineId) {
        Optional<Routine> opt = routineRepo.findByUserIdAndId(userId, routineId);
        if (opt.isEmpty()) return false;

        List<RoutineExercise> exercises = exerciseRepo.findByUserIdAndRoutineId(userId, routineId);
        for (RoutineExercise ex : exercises) {
            intensifierRepo.findByRoutineExerciseId(ex.getId()).ifPresent(intensifierRepo::delete);
        }
        exerciseRepo.deleteAll(exercises);
        routineRepo.delete(opt.get());
        return true;
    }

    @Transactional
    public RoutineExercise addExercise(String userId, String routineId, Map<String, Object> body) {
        if (routineRepo.findByUserIdAndId(userId, routineId).isEmpty()) return null;

        RoutineExercise ex = new RoutineExercise();
        ex.setUserId(userId);
        ex.setRoutineId(routineId);
        populateExerciseFields(ex, body);
        RoutineExercise saved = exerciseRepo.save(ex);

        if (body.containsKey("intensifier") && body.get("intensifier") instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> intMap = (Map<String, Object>) body.get("intensifier");
            saveIntensifier(saved.getId(), intMap);
        }

        return saved;
    }

    @Transactional
    public RoutineExercise updateExercise(String userId, String routineId, Long exerciseId, Map<String, Object> body) {
        RoutineExercise ex = exerciseRepo.findById(exerciseId).orElse(null);
        if (ex == null || !ex.getUserId().equals(userId) || !ex.getRoutineId().equals(routineId)) return null;

        populateExerciseFields(ex, body);
        RoutineExercise saved = exerciseRepo.save(ex);

        if (body.containsKey("intensifier")) {
            intensifierRepo.findByRoutineExerciseId(exerciseId).ifPresent(intensifierRepo::delete);
            if (body.get("intensifier") instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> intMap = (Map<String, Object>) body.get("intensifier");
                saveIntensifier(exerciseId, intMap);
            }
        }

        return saved;
    }

    @Transactional
    public boolean deleteExercise(String userId, String routineId, Long exerciseId) {
        RoutineExercise ex = exerciseRepo.findById(exerciseId).orElse(null);
        if (ex == null || !ex.getUserId().equals(userId) || !ex.getRoutineId().equals(routineId)) return false;

        intensifierRepo.findByRoutineExerciseId(exerciseId).ifPresent(intensifierRepo::delete);
        exerciseRepo.delete(ex);
        return true;
    }

    // ---- Private helpers ----

    /** Save exercises from frontend "ex" array format. */
    private void saveExercisesFromFrontend(String userId, String routineId, List<Map<String, Object>> exList) {
        for (int i = 0; i < exList.size(); i++) {
            Map<String, Object> exData = exList.get(i);
            RoutineExercise ex = new RoutineExercise();
            ex.setUserId(userId);
            ex.setRoutineId(routineId);
            // Frontend uses "id" for exerciseId
            ex.setExerciseId((String) exData.get("id"));
            ex.setSortOrder(i);
            populateExerciseFields(ex, exData);
            RoutineExercise saved = exerciseRepo.save(ex);

            if (exData.containsKey("intensifier") && exData.get("intensifier") instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> intMap = (Map<String, Object>) exData.get("intensifier");
                saveIntensifier(saved.getId(), intMap);
            }
        }
    }

    /** Convert a routine + its exercises to the frontend format with "ex" array. */
    private Map<String, Object> routineToFrontendMap(String userId, Routine r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("name", r.getName());
        m.put("emoji", r.getEmoji());
        m.put("progressionPolicy", r.getProgressionPolicy());
        m.put("excludeFromProgression", r.getExcludeFromProgression());
        m.put("sortOrder", r.getSortOrder());
        m.put("createdAt", r.getCreatedAt());

        List<RoutineExercise> exercises = exerciseRepo.findByUserIdAndRoutineIdOrderBySortOrder(userId, r.getId());
        List<Map<String, Object>> exList = new ArrayList<>();
        for (RoutineExercise ex : exercises) {
            Map<String, Object> exMap = exerciseToFrontendMap(ex);
            intensifierRepo.findByRoutineExerciseId(ex.getId())
                    .ifPresent(intens -> exMap.put("intensifier", intensifierToMap(intens)));
            exList.add(exMap);
        }
        m.put("ex", exList);
        return m;
    }

    /** Convert an exercise to frontend format: uses "id" for exerciseId. */
    private Map<String, Object> exerciseToFrontendMap(RoutineExercise ex) {
        Map<String, Object> m = new LinkedHashMap<>();
        // Frontend expects "id" = the exercise catalog ID
        m.put("id", ex.getExerciseId());
        m.put("sets", ex.getSets());
        m.put("reps", ex.getReps());
        m.put("repsMin", ex.getRepsMin());
        m.put("repsMax", ex.getRepsMax());
        m.put("weight", ex.getWeight());
        m.put("mode", ex.getMode());
        m.put("sec", ex.getSec());
        m.put("minDuration", ex.getMinDuration());
        m.put("speed", ex.getSpeed());
        m.put("bodyweight", ex.getBodyweight());
        m.put("perSide", ex.getPerSide());
        m.put("assisted", ex.getAssisted());
        m.put("restSec", ex.getRestSec());
        m.put("warmupRestSec", ex.getWarmupRestSec());
        m.put("warmupSets", ex.getWarmupSets());
        m.put("progression", ex.getProgression());
        m.put("increment", ex.getIncrement());
        m.put("deloadFactor", ex.getDeloadFactor());
        m.put("note", ex.getNote());
        m.put("sg", ex.getSupersetGroup());
        m.put("sortOrder", ex.getSortOrder());
        return m;
    }

    private Map<String, Object> intensifierToMap(RoutineExerciseIntensifier i) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("type", i.getType());
        m.put("dropCount", i.getDropCount());
        m.put("dropPct", i.getDropPct());
        m.put("totalReps", i.getTotalReps());
        m.put("restSec", i.getRestSec());
        return m;
    }

    private void populateExerciseFields(RoutineExercise ex, Map<String, Object> body) {
        if (body.containsKey("exerciseId"))    ex.setExerciseId((String) body.get("exerciseId"));
        if (body.containsKey("sortOrder"))     ex.setSortOrder(toInt(body.get("sortOrder")));
        if (body.containsKey("sg"))            ex.setSupersetGroup((String) body.get("sg"));
        if (body.containsKey("supersetGroup")) ex.setSupersetGroup((String) body.get("supersetGroup"));
        if (body.containsKey("sets"))          ex.setSets(toInt(body.get("sets")));
        if (body.containsKey("reps"))          ex.setReps(toInt(body.get("reps")));
        if (body.containsKey("repsMin"))       ex.setRepsMin(toInt(body.get("repsMin")));
        if (body.containsKey("repsMax"))       ex.setRepsMax(toInt(body.get("repsMax")));
        if (body.containsKey("weight"))        ex.setWeight(toDouble(body.get("weight")));
        if (body.containsKey("mode"))          ex.setMode((String) body.get("mode"));
        if (body.containsKey("sec"))           ex.setSec(toInt(body.get("sec")));
        if (body.containsKey("minDuration"))   ex.setMinDuration(toInt(body.get("minDuration")));
        if (body.containsKey("speed"))         ex.setSpeed(toDouble(body.get("speed")));
        if (body.containsKey("bodyweight"))    ex.setBodyweight(toInt(body.get("bodyweight")));
        if (body.containsKey("perSide"))       ex.setPerSide(toInt(body.get("perSide")));
        if (body.containsKey("assisted"))      ex.setAssisted(toInt(body.get("assisted")));
        if (body.containsKey("restSec"))       ex.setRestSec(toInt(body.get("restSec")));
        if (body.containsKey("warmupRestSec")) ex.setWarmupRestSec(toInt(body.get("warmupRestSec")));
        if (body.containsKey("warmupSets"))    ex.setWarmupSets(toInt(body.get("warmupSets")));
        if (body.containsKey("progression"))   ex.setProgression((String) body.get("progression"));
        if (body.containsKey("increment"))     ex.setIncrement(toDouble(body.get("increment")));
        if (body.containsKey("deloadFactor"))  ex.setDeloadFactor(toDouble(body.get("deloadFactor")));
        if (body.containsKey("note"))          ex.setNote((String) body.get("note"));

        // Defaults for required fields if creating
        if (ex.getSortOrder() == null)   ex.setSortOrder(0);
        if (ex.getBodyweight() == null)  ex.setBodyweight(0);
        if (ex.getPerSide() == null)     ex.setPerSide(0);
    }

    private void saveIntensifier(Long routineExerciseId, Map<String, Object> intMap) {
        RoutineExerciseIntensifier intens = new RoutineExerciseIntensifier();
        intens.setRoutineExerciseId(routineExerciseId);
        intens.setType((String) intMap.getOrDefault("type", "dropset"));
        intens.setDropCount(toInt(intMap.get("dropCount")));
        intens.setDropPct(toInt(intMap.get("dropPct")));
        intens.setTotalReps(toInt(intMap.get("totalReps")));
        intens.setRestSec(toInt(intMap.get("restSec")));
        intensifierRepo.save(intens);
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
