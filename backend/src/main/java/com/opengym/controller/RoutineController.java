package com.opengym.controller;

import com.opengym.filter.TokenAuthFilter;
import com.opengym.model.Routine;
import com.opengym.model.RoutineExercise;
import com.opengym.model.User;
import com.opengym.service.RoutineService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/routines")
public class RoutineController {

    private final RoutineService routineService;

    public RoutineController(RoutineService routineService) {
        this.routineService = routineService;
    }

    @GetMapping
    public ResponseEntity<?> listRoutines(HttpServletRequest request) {
        User user = TokenAuthFilter.getUser(request);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "not signed in"));
        return ResponseEntity.ok(routineService.listRoutines(user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRoutine(HttpServletRequest request, @PathVariable String id) {
        User user = TokenAuthFilter.getUser(request);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "not signed in"));

        Map<String, Object> detail = routineService.getRoutineDetail(user.getId(), id);
        if (detail == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(detail);
    }

    @PostMapping
    public ResponseEntity<?> createRoutine(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        User user = TokenAuthFilter.getUser(request);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "not signed in"));
        Map<String, Object> r = routineService.createRoutine(user.getId(), body);
        return ResponseEntity.ok(r);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRoutine(HttpServletRequest request, @PathVariable String id,
                                           @RequestBody Map<String, Object> body) {
        User user = TokenAuthFilter.getUser(request);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "not signed in"));

        Map<String, Object> r = routineService.updateRoutine(user.getId(), id, body);
        if (r == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(r);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRoutine(HttpServletRequest request, @PathVariable String id) {
        User user = TokenAuthFilter.getUser(request);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "not signed in"));

        if (!routineService.deleteRoutine(user.getId(), id)) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of("deleted", true));
    }

    @PostMapping("/{id}/exercises")
    public ResponseEntity<?> addExercise(HttpServletRequest request, @PathVariable String id,
                                         @RequestBody Map<String, Object> body) {
        User user = TokenAuthFilter.getUser(request);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "not signed in"));

        RoutineExercise ex = routineService.addExercise(user.getId(), id, body);
        if (ex == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ex);
    }

    @PutMapping("/{routineId}/exercises/{exerciseId}")
    public ResponseEntity<?> updateExercise(HttpServletRequest request,
                                            @PathVariable String routineId,
                                            @PathVariable Long exerciseId,
                                            @RequestBody Map<String, Object> body) {
        User user = TokenAuthFilter.getUser(request);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "not signed in"));

        RoutineExercise ex = routineService.updateExercise(user.getId(), routineId, exerciseId, body);
        if (ex == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ex);
    }

    @DeleteMapping("/{routineId}/exercises/{exerciseId}")
    public ResponseEntity<?> deleteExercise(HttpServletRequest request,
                                            @PathVariable String routineId,
                                            @PathVariable Long exerciseId) {
        User user = TokenAuthFilter.getUser(request);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "not signed in"));

        if (!routineService.deleteExercise(user.getId(), routineId, exerciseId)) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of("deleted", true));
    }
}
