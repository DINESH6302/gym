package com.opengym.service;

import com.opengym.model.*;
import com.opengym.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class WorkoutService {

    private final WorkoutRepository workoutRepo;
    private final WorkoutEntryRepository entryRepo;
    private final WorkoutSetRepository setRepo;
    private final SetDropRepository dropRepo;
    private final SetClusterRepository clusterRepo;
    private final SetSideRepository sideRepo;
    private final WorkoutRoutineRepository workoutRoutineRepo;

    public WorkoutService(WorkoutRepository workoutRepo,
                          WorkoutEntryRepository entryRepo,
                          WorkoutSetRepository setRepo,
                          SetDropRepository dropRepo,
                          SetClusterRepository clusterRepo,
                          SetSideRepository sideRepo,
                          WorkoutRoutineRepository workoutRoutineRepo) {
        this.workoutRepo = workoutRepo;
        this.entryRepo = entryRepo;
        this.setRepo = setRepo;
        this.dropRepo = dropRepo;
        this.clusterRepo = clusterRepo;
        this.sideRepo = sideRepo;
        this.workoutRoutineRepo = workoutRoutineRepo;
    }

    /** List workouts in frontend format with full entries/sets. */
    public List<Map<String, Object>> listWorkouts(String userId, int limit, int offset) {
        List<Workout> all = workoutRepo.findByUserIdOrderByStartTimeDesc(userId);
        int end = Math.min(offset + limit, all.size());
        if (offset >= all.size()) return Collections.emptyList();
        List<Workout> page = all.subList(offset, end);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Workout w : page) {
            result.add(workoutToFrontendMap(userId, w));
        }
        return result;
    }

    /** Get full workout in frontend format. */
    public Map<String, Object> getWorkoutDetail(String userId, String workoutId) {
        Optional<Workout> opt = workoutRepo.findByUserIdAndId(userId, workoutId);
        if (opt.isEmpty()) return null;
        return workoutToFrontendMap(userId, opt.get());
    }

    /** Save a completed workout from frontend format. */
    @Transactional
    @SuppressWarnings("unchecked")
    public Map<String, Object> saveWorkout(String userId, Map<String, Object> body) {
        String workoutId = str(body, "id",
                UUID.randomUUID().toString().replace("-", "").substring(0, 12));

        Workout w = workoutRepo.findByUserIdAndId(userId, workoutId).orElse(new Workout());
        w.setUserId(userId);
        w.setId(workoutId);
        // Frontend sends "d" for date
        w.setDateIso(str(body, "d", str(body, "dateIso", Instant.now().toString().substring(0, 10))));
        // Frontend sends "start"/"end" as timestamps (may be long)
        w.setStartTime(toLong(body.getOrDefault("start", body.get("startTime"))));
        w.setEndTime(toLong(body.getOrDefault("end", body.get("endTime"))));
        w.setName(str(body, "name", null));
        // Frontend sends "bw" for bodyweight
        w.setBodyweight(toDouble(body.getOrDefault("bw", body.get("bodyweight"))));
        w.setNote(str(body, "note", null));
        w.setExcludeFromProgression(toInt(body.getOrDefault("excludeFromProgression", 0)));
        w.setCreatedAt(w.getCreatedAt() != null ? w.getCreatedAt() : Instant.now().toString());
        workoutRepo.save(w);

        // Save workout-routine links
        Object routineIdsObj = body.get("routineIds");
        if (routineIdsObj instanceof List) {
            List<String> routineIds = (List<String>) routineIdsObj;
            List<WorkoutRoutine> oldLinks = workoutRoutineRepo.findByUserIdAndWorkoutId(userId, workoutId);
            workoutRoutineRepo.deleteAll(oldLinks);
            int sortIdx = 0;
            for (String rid : routineIds) {
                if (rid == null) continue;
                WorkoutRoutine wr = new WorkoutRoutine();
                wr.setUserId(userId);
                wr.setWorkoutId(workoutId);
                wr.setRoutineId(rid);
                wr.setSortOrder(sortIdx++);
                workoutRoutineRepo.save(wr);
            }
        }

        // Delete existing entries (replace all)
        List<WorkoutEntry> oldEntries = entryRepo.findByUserIdAndWorkoutId(userId, workoutId);
        for (WorkoutEntry oldEntry : oldEntries) {
            deleteEntryData(oldEntry);
        }
        entryRepo.deleteAll(oldEntries);

        // Process entries
        if (body.containsKey("entries") && body.get("entries") instanceof List) {
            List<Map<String, Object>> entries = (List<Map<String, Object>>) body.get("entries");
            int entryOrder = 0;
            for (Map<String, Object> ed : entries) {
                WorkoutEntry entry = new WorkoutEntry();
                entry.setUserId(userId);
                entry.setWorkoutId(workoutId);
                // Frontend sends "id" for exerciseId in entries
                entry.setExerciseId(str(ed, "id", str(ed, "exerciseId", null)));
                entry.setSortOrder(toInt(ed.getOrDefault("sortOrder", entryOrder)));
                // Frontend sends "topW" for topWeight
                entry.setTopWeight(toDouble(ed.getOrDefault("topW", ed.get("topWeight"))));
                // Frontend sends "rid" for routineId
                entry.setRoutineId(str(ed, "rid", str(ed, "routineId", null)));
                // Frontend sends "noProg" for noProgression
                entry.setNoProgression(toInt(ed.getOrDefault("noProg", ed.getOrDefault("noProgression", 0))));
                entry.setNote(str(ed, "note", null));
                entry.setNotePin(toInt(ed.getOrDefault("notePin", 0)));
                entry.setSupersetGroup(str(ed, "sg", str(ed, "supersetGroup", null)));
                // Target and muscleSnapshot are objects — store as JSON strings
                Object target = ed.get("target");
                if (target != null) entry.setTargetJson(toJsonString(target));
                Object muscleSnapshot = ed.get("muscleSnapshot");
                if (muscleSnapshot != null) entry.setMuscleSnapshotJson(toJsonString(muscleSnapshot));
                entry = entryRepo.save(entry);
                entryOrder++;

                // Process sets
                if (ed.containsKey("sets") && ed.get("sets") instanceof List) {
                    List<Map<String, Object>> sets = (List<Map<String, Object>>) ed.get("sets");
                    int setOrder = 0;
                    for (Map<String, Object> sd : sets) {
                        WorkoutSet ws = new WorkoutSet();
                        ws.setEntryId(entry.getId());
                        ws.setSetOrder(toInt(sd.getOrDefault("setOrder", setOrder)));
                        // Frontend sends "w" for weight
                        ws.setWeight(toDouble(sd.getOrDefault("w", sd.get("weight"))));
                        // Frontend sends "r" for reps
                        ws.setReps(toInt(sd.getOrDefault("r", sd.get("reps"))));
                        ws.setDone(toInt(sd.getOrDefault("done", 0)));
                        ws.setPhase(str(sd, "phase", "work"));
                        ws.setMode(str(sd, "mode", null));
                        ws.setSeconds(toInt(sd.get("seconds")));
                        ws.setMinutes(toDouble(sd.get("minutes")));
                        ws.setSpeed(toDouble(sd.get("speed")));
                        ws.setRir(toDouble(sd.get("rir")));
                        ws.setRpe(toDouble(sd.get("rpe")));
                        ws.setSetType(str(sd, "setType", "normal"));
                        ws.setIsPerSide(toInt(sd.getOrDefault("isPerSide", sd.getOrDefault("perSide", 0))));
                        ws = setRepo.save(ws);
                        setOrder++;

                        // Process drops
                        saveDrops(ws.getId(), null, sd);
                        // Process clusters
                        saveClusters(ws.getId(), null, sd);
                        // Process sides
                        saveSides(ws.getId(), sd);
                    }
                }
            }
        }

        return Map.of("id", workoutId, "saved", true);
    }

    /** Delete a workout and all nested data. */
    @Transactional
    public boolean deleteWorkout(String userId, String workoutId) {
        Optional<Workout> opt = workoutRepo.findByUserIdAndId(userId, workoutId);
        if (opt.isEmpty()) return false;

        List<WorkoutEntry> entries = entryRepo.findByUserIdAndWorkoutId(userId, workoutId);
        for (WorkoutEntry entry : entries) {
            deleteEntryData(entry);
        }
        entryRepo.deleteAll(entries);
        workoutRoutineRepo.deleteAll(workoutRoutineRepo.findByUserIdAndWorkoutId(userId, workoutId));
        workoutRepo.delete(opt.get());
        return true;
    }

    // ---- Private helpers ----

    private void deleteEntryData(WorkoutEntry entry) {
        List<WorkoutSet> sets = setRepo.findByEntryIdOrderBySetOrder(entry.getId());
        for (WorkoutSet ws : sets) {
            List<SetSide> sides = sideRepo.findBySetId(ws.getId());
            for (SetSide side : sides) {
                dropRepo.deleteAll(dropRepo.findBySideId(side.getId()));
                clusterRepo.deleteAll(clusterRepo.findBySideId(side.getId()));
            }
            sideRepo.deleteAll(sides);
            dropRepo.deleteAll(dropRepo.findBySetId(ws.getId()));
            clusterRepo.deleteAll(clusterRepo.findBySetId(ws.getId()));
        }
        setRepo.deleteAll(sets);
    }

    @SuppressWarnings("unchecked")
    private void saveDrops(Long setId, Long sideId, Map<String, Object> data) {
        if (!data.containsKey("drops") || !(data.get("drops") instanceof List)) return;
        List<Map<String, Object>> drops = (List<Map<String, Object>>) data.get("drops");
        int order = 0;
        for (Map<String, Object> dd : drops) {
            SetDrop drop = new SetDrop();
            if (sideId != null) drop.setSideId(sideId);
            else drop.setSetId(setId);
            drop.setDropOrder(toInt(dd.getOrDefault("dropOrder", order)));
            drop.setWeight(toDouble(dd.getOrDefault("w", dd.getOrDefault("weight", 0.0))));
            drop.setReps(toInt(dd.getOrDefault("r", dd.getOrDefault("reps", 0))));
            dropRepo.save(drop);
            order++;
        }
    }

    @SuppressWarnings("unchecked")
    private void saveClusters(Long setId, Long sideId, Map<String, Object> data) {
        if (!data.containsKey("clusters") || !(data.get("clusters") instanceof List)) return;
        List<Map<String, Object>> clusters = (List<Map<String, Object>>) data.get("clusters");
        int order = 0;
        for (Map<String, Object> cd : clusters) {
            SetCluster cluster = new SetCluster();
            if (sideId != null) cluster.setSideId(sideId);
            else cluster.setSetId(setId);
            cluster.setClusterOrder(toInt(cd.getOrDefault("clusterOrder", order)));
            cluster.setReps(toInt(cd.getOrDefault("r", cd.getOrDefault("reps", 0))));
            cluster.setRestSec(toInt(cd.getOrDefault("restSec", 0)));
            clusterRepo.save(cluster);
            order++;
        }
    }

    @SuppressWarnings("unchecked")
    private void saveSides(Long setId, Map<String, Object> setData) {
        // Frontend sends sides as a Map { "R": { w, r, done }, "L": { w, r, done } }
        Object sidesObj = setData.get("sides");
        if (sidesObj == null) return;

        if (sidesObj instanceof Map) {
            Map<String, Object> sidesMap = (Map<String, Object>) sidesObj;
            for (Map.Entry<String, Object> sideEntry : sidesMap.entrySet()) {
                if (!(sideEntry.getValue() instanceof Map)) continue;
                Map<String, Object> sd = (Map<String, Object>) sideEntry.getValue();
                SetSide side = new SetSide();
                side.setSetId(setId);
                side.setSide(sideEntry.getKey());
                side.setWeight(toDouble(sd.getOrDefault("w", sd.get("weight"))));
                side.setReps(toInt(sd.getOrDefault("r", sd.get("reps"))));
                side.setDone(toInt(sd.getOrDefault("done", 0)));
                side.setRir(toDouble(sd.get("rir")));
                side.setRpe(toDouble(sd.get("rpe")));
                side.setSetType(str(sd, "setType", "normal"));
                side = sideRepo.save(side);
                saveDrops(null, side.getId(), sd);
                saveClusters(null, side.getId(), sd);
            }
        } else if (sidesObj instanceof List) {
            List<Map<String, Object>> sidesList = (List<Map<String, Object>>) sidesObj;
            for (Map<String, Object> sd : sidesList) {
                SetSide side = new SetSide();
                side.setSetId(setId);
                side.setSide(str(sd, "side", "L"));
                side.setWeight(toDouble(sd.getOrDefault("w", sd.get("weight"))));
                side.setReps(toInt(sd.getOrDefault("r", sd.get("reps"))));
                side.setDone(toInt(sd.getOrDefault("done", 0)));
                side.setRir(toDouble(sd.get("rir")));
                side.setRpe(toDouble(sd.get("rpe")));
                side.setSetType(str(sd, "setType", "normal"));
                side = sideRepo.save(side);
                saveDrops(null, side.getId(), sd);
                saveClusters(null, side.getId(), sd);
            }
        }
    }

    /** Convert workout + entries + sets to frontend format. */
    private Map<String, Object> workoutToFrontendMap(String userId, Workout w) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", w.getId());
        m.put("d", w.getDateIso());
        m.put("start", w.getStartTime());
        m.put("end", w.getEndTime());
        m.put("name", w.getName());
        m.put("bw", w.getBodyweight());
        m.put("note", w.getNote());
        m.put("excludeFromProgression", w.getExcludeFromProgression());

        // Routine IDs
        List<WorkoutRoutine> routines = workoutRoutineRepo.findByUserIdAndWorkoutIdOrderBySortOrder(userId, w.getId());
        List<String> routineIds = new ArrayList<>();
        for (WorkoutRoutine wr : routines) routineIds.add(wr.getRoutineId());
        m.put("routineIds", routineIds);
        m.put("routineId", routineIds.isEmpty() ? null : routineIds.get(0));

        // Entries
        List<WorkoutEntry> entries = entryRepo.findByUserIdAndWorkoutIdOrderBySortOrder(userId, w.getId());
        List<Map<String, Object>> entryList = new ArrayList<>();
        for (WorkoutEntry entry : entries) {
            Map<String, Object> em = new LinkedHashMap<>();
            em.put("id", entry.getExerciseId());
            em.put("topW", entry.getTopWeight());
            if (entry.getRoutineId() != null) em.put("rid", entry.getRoutineId());
            if (entry.getNoProgression() != null && entry.getNoProgression() == 1) em.put("noProg", true);
            if (entry.getNote() != null) em.put("note", entry.getNote());
            if (entry.getNotePin() != null && entry.getNotePin() == 1) em.put("notePin", true);
            if (entry.getSupersetGroup() != null) em.put("sg", entry.getSupersetGroup());
            if (entry.getTargetJson() != null) em.put("target", parseJson(entry.getTargetJson()));
            if (entry.getMuscleSnapshotJson() != null) em.put("muscleSnapshot", parseJson(entry.getMuscleSnapshotJson()));

            // Sets
            List<WorkoutSet> sets = setRepo.findByEntryIdOrderBySetOrder(entry.getId());
            List<Map<String, Object>> setList = new ArrayList<>();
            for (WorkoutSet ws : sets) {
                Map<String, Object> sm = new LinkedHashMap<>();
                sm.put("w", ws.getWeight());
                sm.put("r", ws.getReps());
                sm.put("done", ws.getDone() != null && ws.getDone() == 1);
                if (ws.getPhase() != null && !"work".equals(ws.getPhase())) sm.put("phase", ws.getPhase());
                if (ws.getMode() != null) sm.put("mode", ws.getMode());
                if (ws.getSeconds() != null) sm.put("seconds", ws.getSeconds());
                if (ws.getMinutes() != null) sm.put("minutes", ws.getMinutes());
                if (ws.getSpeed() != null) sm.put("speed", ws.getSpeed());
                if (ws.getRir() != null) sm.put("rir", ws.getRir());
                if (ws.getRpe() != null) sm.put("rpe", ws.getRpe());
                if (ws.getSetType() != null && !"normal".equals(ws.getSetType())) sm.put("setType", ws.getSetType());
                if (ws.getIsPerSide() != null && ws.getIsPerSide() == 1) sm.put("perSide", true);

                // Drops
                List<SetDrop> drops = dropRepo.findBySetIdOrderByDropOrder(ws.getId());
                if (!drops.isEmpty()) {
                    List<Map<String, Object>> dl = new ArrayList<>();
                    for (SetDrop d : drops) {
                        Map<String, Object> dm = new LinkedHashMap<>();
                        dm.put("w", d.getWeight());
                        dm.put("r", d.getReps());
                        dl.add(dm);
                    }
                    sm.put("drops", dl);
                }

                // Clusters
                List<SetCluster> clusters = clusterRepo.findBySetIdOrderByClusterOrder(ws.getId());
                if (!clusters.isEmpty()) {
                    List<Map<String, Object>> cl = new ArrayList<>();
                    for (SetCluster c : clusters) {
                        Map<String, Object> cm = new LinkedHashMap<>();
                        cm.put("r", c.getReps());
                        cm.put("restSec", c.getRestSec());
                        cl.add(cm);
                    }
                    sm.put("clusters", cl);
                }

                // Sides
                List<SetSide> sides = sideRepo.findBySetId(ws.getId());
                if (!sides.isEmpty()) {
                    Map<String, Object> sidesMap = new LinkedHashMap<>();
                    for (SetSide s : sides) {
                        Map<String, Object> sideM = new LinkedHashMap<>();
                        sideM.put("w", s.getWeight());
                        sideM.put("r", s.getReps());
                        sideM.put("done", s.getDone() != null && s.getDone() == 1);
                        if (s.getRir() != null) sideM.put("rir", s.getRir());
                        if (s.getRpe() != null) sideM.put("rpe", s.getRpe());

                        // Side drops
                        List<SetDrop> sideDrops = dropRepo.findBySideIdOrderByDropOrder(s.getId());
                        if (!sideDrops.isEmpty()) {
                            List<Map<String, Object>> sdl = new ArrayList<>();
                            for (SetDrop d : sideDrops) {
                                Map<String, Object> dm = new LinkedHashMap<>();
                                dm.put("w", d.getWeight());
                                dm.put("r", d.getReps());
                                sdl.add(dm);
                            }
                            sideM.put("drops", sdl);
                        }

                        sidesMap.put(s.getSide(), sideM);
                    }
                    sm.put("sides", sidesMap);
                }

                setList.add(sm);
            }
            em.put("sets", setList);
            entryList.add(em);
        }
        m.put("entries", entryList);
        return m;
    }

    private String str(Map<String, Object> m, String key, String def) {
        Object v = m.get(key);
        return v instanceof String ? (String) v : def;
    }

    private Integer toInt(Object v) {
        if (v == null) return null;
        if (v instanceof Boolean) return ((Boolean) v) ? 1 : 0;
        return ((Number) v).intValue();
    }

    private Long toLong(Object v) {
        if (v == null) return null;
        return ((Number) v).longValue();
    }

    private Double toDouble(Object v) {
        if (v == null) return null;
        if (v instanceof Boolean) return (Boolean) v ? 1.0 : 0.0;
        return ((Number) v).doubleValue();
    }

    private String toJsonString(Object obj) {
        if (obj == null) return null;
        if (obj instanceof String) return (String) obj;
        // Simple conversion for Maps/Lists — use Jackson in production
        try {
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            return om.writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }

    private Object parseJson(String json) {
        if (json == null || json.isEmpty()) return null;
        try {
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            return om.readValue(json, Object.class);
        } catch (Exception e) {
            return json;
        }
    }
}
