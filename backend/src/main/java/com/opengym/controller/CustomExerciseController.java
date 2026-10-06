package com.opengym.controller;

import com.opengym.filter.TokenAuthFilter;
import com.opengym.model.CustomExercise;
import com.opengym.model.User;
import com.opengym.service.CustomExerciseService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/custom-exercises")
public class CustomExerciseController {

    private final CustomExerciseService customExerciseService;

    public CustomExerciseController(CustomExerciseService customExerciseService) {
        this.customExerciseService = customExerciseService;
    }

    @GetMapping
    public ResponseEntity<?> listExercises(HttpServletRequest request) {
        User user = TokenAuthFilter.getUser(request);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "not signed in"));
        return ResponseEntity.ok(customExerciseService.listExercises(user.getId()));
    }

    @PostMapping
    public ResponseEntity<?> createExercise(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        User user = TokenAuthFilter.getUser(request);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "not signed in"));

        if (!body.containsKey("name") || ((String) body.get("name")).trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "name is required"));
        }
        return ResponseEntity.ok(customExerciseService.createExercise(user.getId(), body));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateExercise(HttpServletRequest request, @PathVariable String id,
                                            @RequestBody Map<String, Object> body) {
        User user = TokenAuthFilter.getUser(request);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "not signed in"));

        CustomExercise updated = customExerciseService.updateExercise(user.getId(), id, body);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteExercise(HttpServletRequest request, @PathVariable String id) {
        User user = TokenAuthFilter.getUser(request);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "not signed in"));

        if (!customExerciseService.deleteExercise(user.getId(), id)) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of("deleted", true));
    }
}
