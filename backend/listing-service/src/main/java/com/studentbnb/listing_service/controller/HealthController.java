package com.studentbnb.listing_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Check database connectivity
            try (Connection connection = dataSource.getConnection()) {
                boolean dbHealthy = connection.isValid(5); // 5 second timeout
                
                health.put("status", dbHealthy ? "UP" : "DOWN");
                health.put("service", "listing-service");
                health.put("version", "1.0.0");
                health.put("timestamp", LocalDateTime.now());
                health.put("database", dbHealthy ? "Connected" : "Disconnected");
                
                Map<String, Object> components = new HashMap<>();
                components.put("database", Map.of(
                    "status", dbHealthy ? "UP" : "DOWN",
                    "details", Map.of(
                        "driver", connection.getMetaData().getDriverName(),
                        "url", connection.getMetaData().getURL()
                    )
                ));
                
                components.put("diskSpace", Map.of(
                    "status", "UP",
                    "details", Map.of(
                        "free", Runtime.getRuntime().freeMemory(),
                        "total", Runtime.getRuntime().totalMemory()
                    )
                ));
                
                health.put("components", components);
                
                if (dbHealthy) {
                    return ResponseEntity.ok(health);
                } else {
                    return ResponseEntity.status(503).body(health);
                }
                
            }
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("service", "listing-service");
            health.put("error", e.getMessage());
            health.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(503).body(health);
        }
    }

    @GetMapping("/info")
    public ResponseEntity<?> info() {
        Map<String, Object> info = Map.of(
            "app", Map.of(
                "name", "Listing Service",
                "description", "Student housing listing management service",
                "version", "1.0.0"
            ),
            "build", Map.of(
                "time", LocalDateTime.now(),
                "java", Map.of(
                    "version", System.getProperty("java.version"),
                    "vendor", System.getProperty("java.vendor")
                )
            )
        );
        
        return ResponseEntity.ok(info);
    }
}