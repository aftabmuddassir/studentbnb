package com.studentbnb.user_service.controller;

import com.studentbnb.user_service.dto.CreateUserProfileRequest;
import com.studentbnb.user_service.dto.UserProfileResponse;
import com.studentbnb.user_service.entity.UserRole;
import com.studentbnb.user_service.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "User Service is running");
        response.put("service", "user-service");
        return ResponseEntity.ok(response);
    }

    // Get current user's profile
    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUserProfile() {
        try {
            Long userId = getCurrentUserId();
            
            Optional<UserProfileResponse> profile = userService.getUserProfile(userId);
            if (profile.isPresent()) {
                return ResponseEntity.ok(profile.get());
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch profile: " + e.getMessage()));
        }
    }

    // Create user profile
    @PostMapping("/profile")
    public ResponseEntity<?> createUserProfile(@Valid @RequestBody CreateUserProfileRequest request) {
        try {
            Long userId = getCurrentUserId();
            
            UserProfileResponse profile = userService.createUserProfile(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(profile);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to create profile: " + e.getMessage()));
        }
    }

    // Update user profile
    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(@Valid @RequestBody CreateUserProfileRequest request) {
        try {
            Long userId = getCurrentUserId();
            
            UserProfileResponse profile = userService.updateUserProfile(userId, request);
            return ResponseEntity.ok(profile);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to update profile: " + e.getMessage()));
        }
    }

    // Get user profile by ID (for other users to view)
    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getUserProfile(@PathVariable Long userId) {
        try {
            Optional<UserProfileResponse> profile = userService.getUserProfile(userId);
            if (profile.isPresent()) {
                return ResponseEntity.ok(profile.get());
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch profile: " + e.getMessage()));
        }
    }

    // Search users by role
    @GetMapping("/search/role/{role}")
    public ResponseEntity<?> getUsersByRole(@PathVariable String role) {
        try {
            UserRole userRole = UserRole.valueOf(role.toUpperCase());
            List<UserProfileResponse> users = userService.getUsersByRole(userRole);
            return ResponseEntity.ok(users);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid role: " + role));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to search users: " + e.getMessage()));
        }
    }

    // Search users by university
    @GetMapping("/search/university/{universityName}")
    public ResponseEntity<?> getUsersByUniversity(@PathVariable String universityName) {
        try {
            List<UserProfileResponse> users = userService.getUsersByUniversity(universityName);
            return ResponseEntity.ok(users);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to search users: " + e.getMessage()));
        }
    }

    // Search users by city
    @GetMapping("/search/city/{city}")
    public ResponseEntity<?> getUsersByCity(@PathVariable String city) {
        try {
            List<UserProfileResponse> users = userService.getUsersByCity(city);
            return ResponseEntity.ok(users);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to search users: " + e.getMessage()));
        }
    }

    // Admin endpoints
    @PostMapping("/admin/verify/{userId}")
    public ResponseEntity<?> verifyUser(@PathVariable Long userId) {
        try {
            userService.verifyUser(userId);
            return ResponseEntity.ok(Map.of("message", "User verified successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to verify user: " + e.getMessage()));
        }
    }

    @PostMapping("/admin/deactivate/{userId}")
    public ResponseEntity<?> deactivateUser(@PathVariable Long userId) {
        try {
            userService.deactivateUser(userId);
            return ResponseEntity.ok(Map.of("message", "User deactivated successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to deactivate user: " + e.getMessage()));
        }
    }

    // Get all users (admin only)
    @GetMapping("/admin/all")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<UserProfileResponse> allUsers = userService.getUsersByRole(UserRole.STUDENT);
            allUsers.addAll(userService.getUsersByRole(UserRole.LANDLORD));
            return ResponseEntity.ok(allUsers);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch all users: " + e.getMessage()));
        }
    }

    // Create user endpoint (for service-to-service communication)
    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            String email = request.get("email").toString();
            String role = request.get("role").toString();
            
            UserRole userRole = UserRole.valueOf(role.toUpperCase());
            userService.createUser(userId, email, userRole);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "User created successfully", "userId", userId));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to create user: " + e.getMessage()));
        }
    }

    // Helper method to get current user ID from JWT token
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getCredentials() == null) {
            throw new IllegalStateException("User not authenticated");
        }
        return (Long) authentication.getCredentials();
    }

    // Helper method to get current user email
    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("User not authenticated");
        }
        return authentication.getName();
    }
}