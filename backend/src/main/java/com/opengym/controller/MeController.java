package com.opengym.controller;

import com.opengym.filter.TokenAuthFilter;
import com.opengym.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class MeController {

    @GetMapping("/api/me")
    public ResponseEntity<?> me(HttpServletRequest request) {
        User user = TokenAuthFilter.getUser(request);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "not signed in"));
        }
        return ResponseEntity.ok(Map.of(
            "user", Map.of("id", user.getId(), "name", user.getName(), "email", user.getEmail(), "admin", user.isAdmin())
        ));
    }
}
