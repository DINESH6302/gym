package com.opengym.controller;

import com.opengym.filter.TokenAuthFilter;
import com.opengym.model.User;
import com.opengym.service.WorkoutService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {

    private final WorkoutService workoutService;

    public WorkoutController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    @GetMapping
    public ResponseEntity<?> listWorkouts(HttpServletRequest request,
                                          @RequestParam(defaultValue = "50") int limit,
                                          @RequestParam(defaultValue = "0") int offset) {
        User user = TokenAuthFilter.getUser(request);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "not signed in"));
        return ResponseEntity.ok(workoutService.listWorkouts(user.getId(), limit, offset));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getWorkout(HttpServletRequest request, @PathVariable String id) {
        User user = TokenAuthFilter.getUser(request);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "not signed in"));

        Map<String, Object> detail = workoutService.getWorkoutDetail(user.getId(), id);
        if (detail == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(detail);
    }

    @PostMapping
    public ResponseEntity<?> saveWorkout(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        User user = TokenAuthFilter.getUser(request);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "not signed in"));

        Map<String, Object> result = workoutService.saveWorkout(user.getId(), body);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteWorkout(HttpServletRequest request, @PathVariable String id) {
        User user = TokenAuthFilter.getUser(request);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "not signed in"));

        if (!workoutService.deleteWorkout(user.getId(), id)) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of("deleted", true));
    }
}
