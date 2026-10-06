package com.opengym.controller;

import com.opengym.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /** Create a new account. Returns JWT token. */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String name = (body.getOrDefault("name", "")).trim();
        String email = (body.getOrDefault("email", "")).trim().toLowerCase();
        String password = body.getOrDefault("password", "");

        if (name.isEmpty() || name.length() > 40) {
            return ResponseEntity.badRequest().body(Map.of("error", "Name is required (max 40 chars)"));
        }
        if (email.isEmpty() || !email.contains("@")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Valid email is required"));
        }
        if (password.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password must be at least 6 characters"));
        }

        try {
            String jwt = userService.register(name, email, password);
            return ResponseEntity.ok(Map.of("token", jwt));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
        }
    }

    /** Login with email + password. Returns JWT token. */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = (body.getOrDefault("email", "")).trim().toLowerCase();
        String password = body.getOrDefault("password", "");

        return userService.login(email, password)
                .<ResponseEntity<?>>map(jwt -> ResponseEntity.ok(Map.of("token", jwt)))
                .orElse(ResponseEntity.status(401).body(Map.of("error", "Invalid email or password")));
    }
}
