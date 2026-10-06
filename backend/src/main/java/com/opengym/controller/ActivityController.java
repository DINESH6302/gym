package com.opengym.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Workout activity heartbeat — the frontend pings this every 20s while a workout
 * is active so the server knows there is a live session. Currently a no-op; can
 * be wired to push notifications or presence indicators later.
 */
@RestController
@RequestMapping("/api/activity")
public class ActivityController {

    @PostMapping
    public ResponseEntity<?> postActivity(HttpServletRequest request,
                                          @RequestBody(required = false) Map<String, Object> body) {
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
