package com.opengym.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ConfigController {

    @Value("${opengym.allow-guest:true}")
    private boolean allowGuest;

    @GetMapping("/api/config")
    public Map<String, Object> config() {
        return Map.of("invite_only", false, "allow_guest", allowGuest);
    }
}
