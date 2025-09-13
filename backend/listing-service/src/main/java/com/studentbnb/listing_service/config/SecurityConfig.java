package com.studentbnb.listing_service.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for REST API
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - no authentication required
                .requestMatchers("/api/listings/health", "/api/listings/public/**").permitAll()
                .requestMatchers("/api/listings/search", "/api/listings/nearby", "/api/listings/university/**").permitAll()
                .requestMatchers("/api/listings/{id}").permitAll() // Public listing view
                .requestMatchers("/api/listings").permitAll() // Public listing browse
                
                // Landlord-specific endpoints
                .requestMatchers("/api/listings/my-listings/**").hasRole("LANDLORD")
                .requestMatchers("/api/listings/*/photos").hasRole("LANDLORD")
                .requestMatchers("/api/listings/*/amenities").hasRole("LANDLORD")
                
                // Student-specific endpoints
                .requestMatchers("/api/listings/*/favorite").hasRole("STUDENT")
                .requestMatchers("/api/listings/favorites").hasRole("STUDENT")
                .requestMatchers("/api/listings/*/inquiry").hasRole("STUDENT")
                
                // Landlord and Student endpoints
                .requestMatchers("/api/listings/inquiries/**").hasAnyRole("LANDLORD", "STUDENT")
                
                // Admin only endpoints
                .requestMatchers("/api/listings/admin/**").hasRole("ADMIN")
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write("{\"error\":\"Authentication required\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write("{\"error\":\"Access denied\"}");
                })
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}