package com.marketplace.api_gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

// En el Gateway los controllers también son reactivos
// Devuelven Mono<T> en vez de T directamente
@RestController
public class HealthController {

    @GetMapping("/health")
    public Mono<Map<String, String>> health() {
        return Mono.just(Map.of(
                "status", "ok",
                "service", "api-gateway"
        ));
    }
}