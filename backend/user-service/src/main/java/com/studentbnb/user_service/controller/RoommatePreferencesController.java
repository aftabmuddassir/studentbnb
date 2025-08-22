package com.studentbnb.user_service.controller;

import com.studentbnb.user_service.entity.RoommatePreferences;
import com.studentbnb.user_service.service.RoommatePreferencesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users/preferences")
public class RoommatePreferencesController {

    @Autowired
    private RoommatePreferencesService preferencesService;

    // Get current user's roommate preferences
    @GetMapping
    public ResponseEntity<?> getCurrentUserPreferences() {
        try {
            Long userId = getCurrentUserId();
            
            Optional<RoommatePreferences> preferences = preferencesService.getPreferences(userId);
            if (preferences.isPresent()) {
                return ResponseEntity.ok(preferences.get());
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch preferences: " + e.getMessage()));
        }
    }

    // Create or update roommate preferences
    @PostMapping
    public ResponseEntity<?> savePreferences(@RequestBody RoommatePreferences preferences) {
        try {
            Long userId = getCurrentUserId();
            
            RoommatePreferences savedPreferences = preferencesService.savePreferences(userId, preferences);
            return ResponseEntity.ok(savedPreferences);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to save preferences: " + e.getMessage()));
        }
    }

    // Update roommate preferences
    @PutMapping
    public ResponseEntity<?> updatePreferences(@RequestBody RoommatePreferences preferences) {
        try {
            Long userId = getCurrentUserId();
            
            RoommatePreferences savedPreferences = preferencesService.savePreferences(userId, preferences);
            return ResponseEntity.ok(savedPreferences);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to update preferences: " + e.getMessage()));
        }
    }

    // Delete roommate preferences
    @DeleteMapping
    public ResponseEntity<?> deletePreferences() {
        try {
            Long userId = getCurrentUserId();
            
            preferencesService.deletePreferences(userId);
            return ResponseEntity.ok(Map.of("message", "Preferences deleted successfully"));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to delete preferences: " + e.getMessage()));
        }
    }

    // Find compatible roommates
    @GetMapping("/compatible")
    public ResponseEntity<?> findCompatibleRoommates() {
        try {
            Long userId = getCurrentUserId();
            
            List<RoommatePreferences> compatibleRoommates = preferencesService.findCompatibleRoommates(userId);
            return ResponseEntity.ok(compatibleRoommates);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to find compatible roommates: " + e.getMessage()));
        }
    }

    // Calculate compatibility score with another user
    @GetMapping("/compatibility/{otherUserId}")
    public ResponseEntity<?> calculateCompatibility(@PathVariable Long otherUserId) {
        try {
            Long userId = getCurrentUserId();
            
            double score = preferencesService.calculateCompatibilityScore(userId, otherUserId);
            
            Map<String, Object> response = Map.of(
                "userId1", userId,
                "userId2", otherUserId,
                "compatibilityScore", score,
                "compatibilityLevel", getCompatibilityLevel(score)
            );
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to calculate compatibility: " + e.getMessage()));
        }
    }

    // Search by budget range
    @GetMapping("/search/budget")
    public ResponseEntity<?> searchByBudget(@RequestParam Integer minBudget, @RequestParam Integer maxBudget) {
        try {
            List<RoommatePreferences> preferences = preferencesService.findByBudgetRange(minBudget, maxBudget);
            return ResponseEntity.ok(preferences);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to search by budget: " + e.getMessage()));
        }
    }

    // Search by smoking preference
    @GetMapping("/search/smoking/{smoking}")
    public ResponseEntity<?> searchBySmoking(@PathVariable Boolean smoking) {
        try {
            List<RoommatePreferences> preferences = preferencesService.findBySmokingPreference(smoking);
            return ResponseEntity.ok(preferences);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to search by smoking preference: " + e.getMessage()));
        }
    }

    // Search by pets allowed
    @GetMapping("/search/pets/{petsAllowed}")
    public ResponseEntity<?> searchByPets(@PathVariable Boolean petsAllowed) {
        try {
            List<RoommatePreferences> preferences = preferencesService.findByPetsAllowed(petsAllowed);
            return ResponseEntity.ok(preferences);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to search by pets preference: " + e.getMessage()));
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

    // Helper method to get compatibility level description
    private String getCompatibilityLevel(double score) {
        if (score >= 80) return "Excellent";
        if (score >= 60) return "Good";
        if (score >= 40) return "Fair";
        if (score >= 20) return "Poor";
        return "Very Poor";
    }
}