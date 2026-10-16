package com.hotel.controller;

import java.util.Map;
import java.net.URI;
import com.hotel.config.FrontendProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    private final URI frontendUrl;

    public HealthController(FrontendProperties frontendProperties) {
        this.frontendUrl = frontendProperties.url();
    }

    @GetMapping("/")
    public ResponseEntity<Void> home() {
        return ResponseEntity.status(HttpStatus.FOUND).location(frontendUrl).build();
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "message", "Sistema de Reservas de Hotel activo.");
    }
}
