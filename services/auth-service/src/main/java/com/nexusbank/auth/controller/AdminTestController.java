package com.nexusbank.auth.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AdminTestController {

    @GetMapping("/api/admin/test")
    public Map<String, Object> test(Authentication authentication) {

        return Map.of(
                "message", "Admin authorization successful",
                "username", authentication.getName(),
                "authorities", authentication.getAuthorities()
        );
    }
}