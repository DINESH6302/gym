package com.opengym.controller;

import com.opengym.filter.TokenAuthFilter;
import com.opengym.model.User;
import com.opengym.service.ExerciseDataService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ExerciseDataController {

    private final ExerciseDataService exerciseDataService;

    public ExerciseDataController(ExerciseDataService exerciseDataService) {
        this.exerciseDataService = exerciseDataService;
    }

    // ---- Exercise Weights ----

    @GetMapping("/exercise-weights")
    public ResponseEntity<?> getExerciseWeights(HttpServletRequest request) {
        User user = TokenAuthFilter.getUser(request);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "not signed in"));
        return ResponseEntity.ok(exerciseDataService.getAllWeightsMap(user.getId()));
    }

    @PutMapping("/exercise-weights/{exerciseId}")
    public ResponseEntity<?> setExerciseWeight(HttpServletRequest request,
                                               @PathVariable String exerciseId,
                                               @RequestBody Map<String, Object> body) {
        User user = TokenAuthFilter.getUser(request);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "not signed in"));

        if (!body.containsKey("weight")) {
            return ResponseEntity.badRequest().body(Map.of("error", "weight is required"));
        }
        return ResponseEntity.ok(exerciseDataService.setWeight(user.getId(), exerciseId, body));
    }

    // ---- Exercise Notes ----

    @GetMapping("/exercise-notes")
    public ResponseEntity<?> getExerciseNotes(HttpServletRequest request) {
        User user = TokenAuthFilter.getUser(request);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "not signed in"));
        return ResponseEntity.ok(exerciseDataService.getAllNotesMap(user.getId()));
    }

    @PutMapping("/exercise-notes/{exerciseId}")
    public ResponseEntity<?> setExerciseNote(HttpServletRequest request,
                                             @PathVariable String exerciseId,
                                             @RequestBody Map<String, Object> body) {
        User user = TokenAuthFilter.getUser(request);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "not signed in"));

        if (!body.containsKey("note")) {
            return ResponseEntity.badRequest().body(Map.of("error", "note is required"));
        }
        return ResponseEntity.ok(exerciseDataService.setNote(user.getId(), exerciseId, body));
    }

    // ---- Favourites ----

    @GetMapping("/favourites")
    public ResponseEntity<?> getFavourites(HttpServletRequest request) {
        User user = TokenAuthFilter.getUser(request);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "not signed in"));
        return ResponseEntity.ok(exerciseDataService.getFavourites(user.getId()));
    }

    @PostMapping("/favourites/{exerciseId}")
    public ResponseEntity<?> addFavourite(HttpServletRequest request, @PathVariable String exerciseId) {
        User user = TokenAuthFilter.getUser(request);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "not signed in"));
        exerciseDataService.addFavourite(user.getId(), exerciseId);
        return ResponseEntity.ok(Map.of("added", true));
    }

    @DeleteMapping("/favourites/{exerciseId}")
    public ResponseEntity<?> removeFavourite(HttpServletRequest request, @PathVariable String exerciseId) {
        User user = TokenAuthFilter.getUser(request);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "not signed in"));

        if (!exerciseDataService.removeFavourite(user.getId(), exerciseId)) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of("removed", true));
    }

    // ---- Bar Weights ----

    @GetMapping("/bar-weights")
    public ResponseEntity<?> getBarWeights(HttpServletRequest request) {
        User user = TokenAuthFilter.getUser(request);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "not signed in"));
        return ResponseEntity.ok(exerciseDataService.getAllBarWeightsMap(user.getId()));
    }

    @PutMapping("/bar-weights/{exerciseId}")
    public ResponseEntity<?> setBarWeight(HttpServletRequest request,
                                          @PathVariable String exerciseId,
                                          @RequestBody Map<String, Object> body) {
        User user = TokenAuthFilter.getUser(request);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "not signed in"));

        if (!body.containsKey("weight")) {
            return ResponseEntity.badRequest().body(Map.of("error", "weight is required"));
        }
        return ResponseEntity.ok(exerciseDataService.setBarWeight(user.getId(), exerciseId, body));
    }
}
