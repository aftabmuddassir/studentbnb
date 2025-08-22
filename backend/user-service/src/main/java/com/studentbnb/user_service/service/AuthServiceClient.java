package com.studentbnb.user_service.service;

import com.studentbnb.user_service.entity.User;
import com.studentbnb.user_service.entity.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

@Service
public class AuthServiceClient {

    @Value("${auth.service.url}")
    private String authServiceUrl;

    @Autowired
    private UserService userService;

    private final WebClient webClient;

    public AuthServiceClient() {
        this.webClient = WebClient.builder().build();
    }

    // Method to sync user from auth service to user service
    public void syncUserFromAuth(Long userId, String email, String role) {
        try {
            UserRole userRole = UserRole.valueOf(role.toUpperCase());
            
            // Check if user already exists in user service
            if (!userService.findUserById(userId).isPresent()) {
                // Create user in user service
                userService.createUser(userId, email, userRole);
                System.out.println("User synced from auth service: " + email);
            }
            
        } catch (Exception e) {
            System.err.println("Failed to sync user from auth service: " + e.getMessage());
        }
    }

    // Method to validate user exists in auth service
    public boolean validateUserInAuthService(String email) {
        try {
            ResponseEntity<Map> response = webClient
                .get()
                .uri(authServiceUrl + "/api/auth/validate-user?email=" + email)
                .retrieve()
                .toEntity(Map.class)
                .block();

            return response != null && response.getStatusCode() == HttpStatus.OK;
            
        } catch (WebClientResponseException e) {
            System.err.println("Error validating user in auth service: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Failed to connect to auth service: " + e.getMessage());
            return false;
        }
    }

    // Method to notify auth service about profile completion
    public void notifyProfileCompletion(Long userId, boolean isCompleted) {
        try {
            Map<String, Object> request = Map.of(
                "userId", userId,
                "profileCompleted", isCompleted
            );

            webClient
                .post()
                .uri(authServiceUrl + "/api/auth/profile-status")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block();

            System.out.println("Notified auth service about profile completion for user: " + userId);
            
        } catch (Exception e) {
            System.err.println("Failed to notify auth service about profile completion: " + e.getMessage());
        }
    }

    // Health check for auth service
    public boolean isAuthServiceHealthy() {
        try {
            ResponseEntity<String> response = webClient
                .get()
                .uri(authServiceUrl + "/api/auth/health")
                .retrieve()
                .toEntity(String.class)
                .block();

            return response != null && response.getStatusCode() == HttpStatus.OK;
            
        } catch (Exception e) {
            System.err.println("Auth service health check failed: " + e.getMessage());
            return false;
        }
    }
}