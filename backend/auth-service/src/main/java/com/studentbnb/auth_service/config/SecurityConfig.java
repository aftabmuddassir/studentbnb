package com.studentbnb.auth_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for development
            .authorizeHttpRequests(authz -> authz
            .anyRequest().permitAll()
                // .requestMatchers("/", "/api/test", "/actuator/**","/api/auth/register", "/api/auth/login").permitAll() // Allow these endpoints
                // .anyRequest().authenticated() // All other requests need authentication
            );

        return http.build();
    }
}