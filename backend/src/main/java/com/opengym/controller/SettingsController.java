package com.opengym.controller;

import com.opengym.filter.TokenAuthFilter;
import com.opengym.model.User;
import com.opengym.model.UserSettings;
import com.opengym.service.SettingsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public ResponseEntity<?> getSettings(HttpServletRequest request) {
        User user = TokenAuthFilter.getUser(request);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "not signed in"));

        Optional<UserSettings> settings = settingsService.getSettings(user.getId());
        if (settings.isEmpty()) {
            // Auto-create defaults if missing
            UserSettings defaults = settingsService.createDefaults(user.getId());
            return ResponseEntity.ok(defaults);
        }
        return ResponseEntity.ok(settings.get());
    }

    @PutMapping
    public ResponseEntity<?> updateSettings(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        User user = TokenAuthFilter.getUser(request);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "not signed in"));

        try {
            UserSettings updated = settingsService.updateSettings(user.getId(), body);
            return ResponseEntity.ok(updated);
        } catch (IllegalStateException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
