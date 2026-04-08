package com.marketplace.user_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    // @GetMapping mapea peticiones GET a este método
    // Spring automáticamente serializa el Map a JSON
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
            "status", "ok",
            "service", "user-service"
        );
    }
}