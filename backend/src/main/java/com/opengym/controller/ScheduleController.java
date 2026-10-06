package com.opengym.controller;

import com.opengym.filter.TokenAuthFilter;
import com.opengym.model.User;
import com.opengym.service.ScheduleService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping
    public ResponseEntity<?> getSchedule(HttpServletRequest request) {
        User user = TokenAuthFilter.getUser(request);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "not signed in"));
        return ResponseEntity.ok(scheduleService.getSchedule(user.getId()));
    }

    @PutMapping("/week")
    public ResponseEntity<?> replaceWeekSchedule(HttpServletRequest request,
                                                 @RequestBody Object body) {
        User user = TokenAuthFilter.getUser(request);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "not signed in"));

        scheduleService.replaceWeekSchedule(user.getId(), body);
        return ResponseEntity.ok(Map.of("saved", true));
    }

    @PutMapping("/day/{dateIso}")
    public ResponseEntity<?> setDayOverride(HttpServletRequest request,
                                            @PathVariable String dateIso,
                                            @RequestBody(required = false) Map<String, Object> body) {
        User user = TokenAuthFilter.getUser(request);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "not signed in"));

        scheduleService.setDayOverride(user.getId(), dateIso, body);
        return ResponseEntity.ok(Map.of("saved", true));
    }
}
