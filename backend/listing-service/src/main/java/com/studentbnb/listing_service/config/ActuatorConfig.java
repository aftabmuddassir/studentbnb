package com.studentbnb.listing_service.config;


// import org.springframework.boot.actuator.health.Health;
// import org.springframework.boot.actuator.health.HealthIndicator;
// import org.springframework.boot.actuator.autoconfigure.health.HealthEndpointAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActuatorConfig {

    // @Bean
    // public HealthIndicator listingServiceHealthIndicator() {
    //     return new HealthIndicator() {
    //         @Override
    //         public Health health() {
    //             // Add custom health checks here
    //             return Health.up()
    //                     .withDetail("service", "listing-service")
    //                     .withDetail("status", "running")
    //                     .withDetail("version", "1.0.0")
    //                     .build();
    //         }
    //     };
    // }
}