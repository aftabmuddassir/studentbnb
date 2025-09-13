package com.studentbnb.listing_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.studentbnb.listing_service.repository")
@EnableTransactionManagement
public class DatabaseConfig {
    
    // Additional DB configuration can be added here if needed
    // For now, Spring Boot's auto-configuration handles most of the setup
}