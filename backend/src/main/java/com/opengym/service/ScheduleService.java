package com.opengym.service;

import com.opengym.model.DayPlanOverride;
import com.opengym.model.WeekSchedule;
import com.opengym.repository.DayPlanOverrideRepository;
import com.opengym.repository.WeekScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ScheduleService {

    private final WeekScheduleRepository weekRepo;
    private final DayPlanOverrideRepository dayRepo;

    public ScheduleService(WeekScheduleRepository weekRepo, DayPlanOverrideRepository dayRepo) {
        this.weekRepo = weekRepo;
        this.dayRepo = dayRepo;
    }

    /**
     * Get the full schedule in frontend format:
     * { week: { 0: ["routineId1"], 1: ["routineId2"] }, dayOverrides: { "2024-01-15": "routineId" } }
     */
    public Map<String, Object> getSchedule(String userId) {
        List<WeekSchedule> weekEntries = weekRepo.findByUserIdOrderByWeekdayAscSortOrderAsc(userId);
        List<DayPlanOverride> overrides = dayRepo.findByUserId(userId);

        // Build week map: weekday (int) -> list of routine IDs
        Map<String, List<String>> weekMap = new LinkedHashMap<>();
        for (WeekSchedule ws : weekEntries) {
            String key = String.valueOf(ws.getWeekday());
            weekMap.computeIfAbsent(key, k -> new ArrayList<>()).add(ws.getRoutineId());
        }

        // Build day overrides map: dateIso -> routineId
        Map<String, String> overrideMap = new LinkedHashMap<>();
        for (DayPlanOverride ov : overrides) {
            overrideMap.put(ov.getDateIso(), ov.getRoutineId());
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("week", weekMap);
        result.put("dayOverrides", overrideMap);
        return result;
    }

    /**
     * Replace the entire week schedule.
     * Accepts the frontend format: { "0": ["routineId1"], "1": ["routineId2", "routineId3"] }
     */
    @Transactional
    @SuppressWarnings("unchecked")
    public void replaceWeekSchedule(String userId, Object body) {
        // Delete all existing
        List<WeekSchedule> existing = weekRepo.findByUserId(userId);
        weekRepo.deleteAll(existing);

        if (body instanceof Map) {
            // Frontend sends: { "0": ["rid1"], "1": ["rid2"] } or { 0: ["rid1"] }
            Map<String, Object> weekMap = (Map<String, Object>) body;
            for (Map.Entry<String, Object> entry : weekMap.entrySet()) {
                int weekday = Integer.parseInt(entry.getKey());
                Object val = entry.getValue();
                if (val instanceof List) {
                    List<String> routineIds = (List<String>) val;
                    for (int i = 0; i < routineIds.size(); i++) {
                        WeekSchedule ws = new WeekSchedule();
                        ws.setUserId(userId);
                        ws.setWeekday(weekday);
                        ws.setRoutineId(routineIds.get(i));
                        ws.setSortOrder(i);
                        weekRepo.save(ws);
                    }
                } else if (val instanceof String) {
                    // Single routine ID without wrapping in array
                    WeekSchedule ws = new WeekSchedule();
                    ws.setUserId(userId);
                    ws.setWeekday(weekday);
                    ws.setRoutineId((String) val);
                    ws.setSortOrder(0);
                    weekRepo.save(ws);
                }
            }
        } else if (body instanceof List) {
            // Legacy format: [{ weekday, routineId, sortOrder }]
            List<Map<String, Object>> entries = (List<Map<String, Object>>) body;
            for (Map<String, Object> entry : entries) {
                WeekSchedule ws = new WeekSchedule();
                ws.setUserId(userId);
                ws.setWeekday(((Number) entry.get("weekday")).intValue());
                ws.setRoutineId((String) entry.get("routineId"));
                ws.setSortOrder(entry.containsKey("sortOrder") ? ((Number) entry.get("sortOrder")).intValue() : 0);
                weekRepo.save(ws);
            }
        }
    }

    /** Set or clear a day override. If routineId is null or empty, delete the override. */
    @Transactional
    public void setDayOverride(String userId, String dateIso, Map<String, Object> body) {
        String routineId = body != null ? (String) body.get("routineId") : null;

        Optional<DayPlanOverride> existing = dayRepo.findByUserIdAndDateIso(userId, dateIso);

        if (routineId == null || routineId.isEmpty()) {
            // Clear/delete override
            existing.ifPresent(dayRepo::delete);
        } else {
            DayPlanOverride ov = existing.orElse(new DayPlanOverride());
            ov.setUserId(userId);
            ov.setDateIso(dateIso);
            ov.setRoutineId(routineId);
            dayRepo.save(ov);
        }
    }
}
