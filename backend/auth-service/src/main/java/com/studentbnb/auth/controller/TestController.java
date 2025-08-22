package com.studentbnb.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import java.util.Map;

@RestController
public class TestController {

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> home() {
        return ResponseEntity.ok(Map.of(
            "message", "Studentbnb Auth Service is running!",
            "version", "1.0.0",
            "status", "active"
        ));
    }

    @GetMapping("/api/test")
    public ResponseEntity<Map<String, String>> test() {
        return ResponseEntity.ok(Map.of(
            "service", "auth-service",
            "message", "API is working correctly!",
            "timestamp", java.time.Instant.now().toString()
        ));
    }
}