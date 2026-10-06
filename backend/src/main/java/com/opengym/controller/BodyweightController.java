package com.opengym.controller;

import com.opengym.filter.TokenAuthFilter;
import com.opengym.model.BodyweightEntry;
import com.opengym.model.User;
import com.opengym.service.BodyweightService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/bodyweight")
public class BodyweightController {

    private final BodyweightService bodyweightService;

    public BodyweightController(BodyweightService bodyweightService) {
        this.bodyweightService = bodyweightService;
    }

    @GetMapping
    public ResponseEntity<?> listEntries(HttpServletRequest request) {
        User user = TokenAuthFilter.getUser(request);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "not signed in"));
        return ResponseEntity.ok(bodyweightService.listEntries(user.getId()));
    }

    @PostMapping
    public ResponseEntity<?> addEntry(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        User user = TokenAuthFilter.getUser(request);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "not signed in"));

        // Frontend uses "w" for weight, "d" for date, "t" for timestamp
        if (body.containsKey("w") && !body.containsKey("weight")) {
            body.put("weight", body.get("w"));
        }
        if (body.containsKey("d") && !body.containsKey("date")) {
            body.put("date", body.get("d"));
        }
        if (!body.containsKey("weight")) {
            return ResponseEntity.badRequest().body(Map.of("error", "weight is required"));
        }

        Map<String, Object> entry = bodyweightService.addEntry(user.getId(), body);
        return ResponseEntity.ok(entry);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEntry(HttpServletRequest request, @PathVariable Long id) {
        User user = TokenAuthFilter.getUser(request);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "not signed in"));

        if (!bodyweightService.deleteEntry(user.getId(), id)) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of("deleted", true));
    }
}
