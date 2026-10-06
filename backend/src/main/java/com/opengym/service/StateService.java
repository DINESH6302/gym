package com.opengym.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.opengym.model.UserState;
import com.opengym.repository.UserStateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Service
public class StateService {

    private final UserStateRepository stateRepo;
    private final ObjectMapper mapper;

    public StateService(UserStateRepository stateRepo, ObjectMapper mapper) {
        this.stateRepo = stateRepo;
        this.mapper = mapper;
    }

    /** Read user state. Returns {state, rev}. */
    public Map<String, Object> getState(String userId) {
        Optional<UserState> opt = stateRepo.findById(userId);
        if (opt.isEmpty()) return Map.of("state", Map.of(), "rev", 0);
        UserState us = opt.get();
        try {
            JsonNode state = mapper.readTree(us.getStateJson());
            return Map.of("state", state, "rev", us.getRev());
        } catch (Exception e) {
            return Map.of("state", Map.of(), "rev", us.getRev());
        }
    }

    /** Get just the revision number. */
    public int getRev(String userId) {
        return stateRepo.findById(userId).map(UserState::getRev).orElse(0);
    }

    /**
     * Update user state with optimistic concurrency.
     * Returns a result map: {ok, ts, rev} on success, or {error, rev, state} on conflict.
     */
    @Transactional
    public Map<String, Object> updateState(String userId, JsonNode state, Integer baseRev) {
        UserState current = stateRepo.findById(userId).orElse(null);
        int currentRev = (current != null) ? current.getRev() : 0;

        // Optimistic concurrency check
        if (baseRev != null && baseRev != currentRev) {
            try {
                JsonNode currentState = (current != null) ? mapper.readTree(current.getStateJson()) : mapper.createObjectNode();
                return Map.of("error", "conflict", "rev", currentRev, "state", currentState);
            } catch (Exception e) {
                return Map.of("error", "conflict", "rev", currentRev, "state", Map.of());
            }
        }

        // Sanitize state
        ObjectNode obj = (ObjectNode) state;
        obj.remove("active"); // in-progress workouts stay device-local
        int newRev = currentRev + 1;
        obj.put("_rev", newRev);

        // Filter non-object entries from arrays
        filterRecords(obj, "workouts");
        filterRecords(obj, "routines");

        String stateJson = obj.toString();
        String now = Instant.now().toString();

        if (current == null) {
            UserState ns = new UserState(userId);
            ns.setStateJson(stateJson);
            ns.setRev(newRev);
            ns.setUpdatedAt(now);
            stateRepo.save(ns);
        } else {
            current.setStateJson(stateJson);
            current.setRev(newRev);
            current.setUpdatedAt(now);
            stateRepo.save(current);
        }

        Long ts = obj.has("_ts") && !obj.get("_ts").isNull() ? obj.get("_ts").asLong() : null;
        return Map.of("ok", true, "ts", ts != null ? ts : "", "rev", newRev);
    }

    private void filterRecords(ObjectNode obj, String field) {
        if (!obj.has(field) || !obj.get(field).isArray()) return;
        ArrayNode arr = (ArrayNode) obj.get(field);
        ArrayNode filtered = mapper.createArrayNode();
        for (JsonNode item : arr) {
            if (item.isObject()) filtered.add(item);
        }
        obj.set(field, filtered);
    }
}
