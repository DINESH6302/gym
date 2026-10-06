package com.opengym.controller;

import com.opengym.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    private final UserService userService;

    public HealthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/api/health")
    public Map<String, Object> health() {
        return Map.of("ok", true, "users", userService.userCount());
    }
}
