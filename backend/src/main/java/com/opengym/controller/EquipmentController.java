package com.opengym.controller;

import com.opengym.filter.TokenAuthFilter;
import com.opengym.model.User;
import com.opengym.service.EquipmentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/equipment-profiles")
public class EquipmentController {

    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @GetMapping
    public ResponseEntity<?> listProfiles(HttpServletRequest request) {
        User user = TokenAuthFilter.getUser(request);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "not signed in"));
        return ResponseEntity.ok(equipmentService.listProfiles(user.getId()));
    }

    @PostMapping
    public ResponseEntity<?> createProfile(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        User user = TokenAuthFilter.getUser(request);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "not signed in"));
        return ResponseEntity.ok(equipmentService.createProfile(user.getId(), body));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProfile(HttpServletRequest request, @PathVariable String id,
                                           @RequestBody Map<String, Object> body) {
        User user = TokenAuthFilter.getUser(request);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "not signed in"));

        Map<String, Object> result = equipmentService.updateProfile(user.getId(), id, body);
        if (result == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProfile(HttpServletRequest request, @PathVariable String id) {
        User user = TokenAuthFilter.getUser(request);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "not signed in"));

        if (!equipmentService.deleteProfile(user.getId(), id)) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of("deleted", true));
    }
}
