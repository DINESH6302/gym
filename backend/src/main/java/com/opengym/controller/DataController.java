package com.opengym.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.opengym.filter.TokenAuthFilter;
import com.opengym.model.User;
import com.opengym.service.StateService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/data")
public class DataController {

    private final StateService stateService;

    public DataController(StateService stateService) {
        this.stateService = stateService;
    }

    /** Get the full user state. */
    @GetMapping
    public ResponseEntity<?> getData(HttpServletRequest request) {
        User user = TokenAuthFilter.getUser(request);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "not signed in"));
        return ResponseEntity.ok(stateService.getState(user.getId()));
    }

    /** Get just the revision number (lightweight poll). */
    @GetMapping("/rev")
    public ResponseEntity<?> getRev(HttpServletRequest request) {
        User user = TokenAuthFilter.getUser(request);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "not signed in"));
        return ResponseEntity.ok(Map.of("rev", stateService.getRev(user.getId())));
    }

    /** Update user state with optimistic concurrency. */
    @PutMapping
    public ResponseEntity<?> putData(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        User user = TokenAuthFilter.getUser(request);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "not signed in"));

        Object stateObj = body.get("state");
        if (stateObj == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "state required"));
        }

        // Convert state to JsonNode
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        JsonNode state = mapper.valueToTree(stateObj);

        if (!state.isObject()) {
            return ResponseEntity.badRequest().body(Map.of("error", "state must be an object"));
        }

        Integer baseRev = body.containsKey("baseRev") && body.get("baseRev") != null
            ? ((Number) body.get("baseRev")).intValue()
            : null;

        Map<String, Object> result = stateService.updateState(user.getId(), state, baseRev);

        if (result.containsKey("error")) {
            return ResponseEntity.status(409).body(result);
        }
        return ResponseEntity.ok(result);
    }
}
