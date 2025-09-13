package com.studentbnb.listing_service.controller;



import com.studentbnb.listing_service.dto.ErrorResponse;
import com.studentbnb.listing_service.dto.ListingResponse;
import com.studentbnb.listing_service.dto.SuccessResponse;
import com.studentbnb.listing_service.entity.ListingFavorite;
import com.studentbnb.listing_service.service.ListingFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/listings")
public class ListingFavoriteController {

    @Autowired
    private ListingFavoriteService favoriteService;

    // Add listing to favorites (Students only)
    @PostMapping("/{listingId}/favorite")
    public ResponseEntity<?> addToFavorites(@PathVariable Long listingId) {
        try {
            Long userId = getCurrentUserId();
            ListingFavorite favorite = favoriteService.addToFavorites(listingId, userId);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse("Listing added to favorites", favorite));
                
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Bad Request", e.getMessage(), 400, "/api/listings/" + listingId + "/favorite"));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to add to favorites: " + e.getMessage(), 500, "/api/listings/" + listingId + "/favorite"));
        }
    }

    // Remove listing from favorites (Students only)
    @DeleteMapping("/{listingId}/favorite")
    public ResponseEntity<?> removeFromFavorites(@PathVariable Long listingId) {
        try {
            Long userId = getCurrentUserId();
            favoriteService.removeFromFavorites(listingId, userId);
            
            return ResponseEntity.ok(new SuccessResponse("Listing removed from favorites"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Bad Request", e.getMessage(), 400, "/api/listings/" + listingId + "/favorite"));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to remove from favorites: " + e.getMessage(), 500, "/api/listings/" + listingId + "/favorite"));
        }
    }

    // Check if listing is favorited (Students only)
    @GetMapping("/{listingId}/favorite/status")
    public ResponseEntity<?> checkFavoriteStatus(@PathVariable Long listingId) {
        try {
            Long userId = getCurrentUserId();
            boolean isFavorited = favoriteService.isListingFavorited(listingId, userId);
            
            return ResponseEntity.ok(Map.of(
                "listingId", listingId,
                "isFavorited", isFavorited
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to check favorite status: " + e.getMessage(), 500, "/api/listings/" + listingId + "/favorite/status"));
        }
    }

    // Get user's favorite listings (Students only)
    @GetMapping("/favorites")
    public ResponseEntity<?> getUserFavorites(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Long userId = getCurrentUserId();
            
            if (page == -1) {
                // Return all favorites without pagination
                List<ListingResponse> favorites = favoriteService.getUserFavorites(userId);
                return ResponseEntity.ok(favorites);
            } else {
                // Return paginated favorites
                Page<ListingResponse> favorites = favoriteService.getUserFavoritesPaginated(userId, page, size, sortBy, sortDir);
                return ResponseEntity.ok(favorites);
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to fetch favorites: " + e.getMessage(), 500, "/api/listings/favorites"));
        }
    }

    // Get favorite count for user (Students only)
    @GetMapping("/favorites/count")
    public ResponseEntity<?> getUserFavoriteCount() {
        try {
            Long userId = getCurrentUserId();
            Long count = favoriteService.getUserFavoriteCount(userId);
            
            return ResponseEntity.ok(Map.of(
                "userId", userId,
                "favoriteCount", count
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to fetch favorite count: " + e.getMessage(), 500, "/api/listings/favorites/count"));
        }
    }

    // Bulk add to favorites (Students only)
    @PostMapping("/favorites/bulk")
    public ResponseEntity<?> addMultipleToFavorites(@RequestBody Map<String, List<Long>> request) {
        try {
            Long userId = getCurrentUserId();
            List<Long> listingIds = request.get("listingIds");
            
            if (listingIds == null || listingIds.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Bad Request", "Listing IDs are required", 400, "/api/listings/favorites/bulk"));
            }
            
            List<ListingFavorite> favorites = favoriteService.addMultipleToFavorites(listingIds, userId);
            
            return ResponseEntity.ok(new SuccessResponse("Added " + favorites.size() + " listings to favorites", favorites));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to add multiple favorites: " + e.getMessage(), 500, "/api/listings/favorites/bulk"));
        }
    }

    // Bulk remove from favorites (Students only)
    @DeleteMapping("/favorites/bulk")
    public ResponseEntity<?> removeMultipleFromFavorites(@RequestBody Map<String, List<Long>> request) {
        try {
            Long userId = getCurrentUserId();
            List<Long> listingIds = request.get("listingIds");
            
            if (listingIds == null || listingIds.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Bad Request", "Listing IDs are required", 400, "/api/listings/favorites/bulk"));
            }
            
            favoriteService.removeMultipleFromFavorites(listingIds, userId);
            
            return ResponseEntity.ok(new SuccessResponse("Removed listings from favorites"));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to remove multiple favorites: " + e.getMessage(), 500, "/api/listings/favorites/bulk"));
        }
    }

    // Clear all favorites (Students only)
    @DeleteMapping("/favorites")
    public ResponseEntity<?> clearAllFavorites() {
        try {
            Long userId = getCurrentUserId();
            favoriteService.clearAllFavorites(userId);
            
            return ResponseEntity.ok(new SuccessResponse("All favorites cleared"));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to clear favorites: " + e.getMessage(), 500, "/api/listings/favorites"));
        }
    }

    // Helper method
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getCredentials() == null) {
            throw new IllegalStateException("User not authenticated");
        }
        return (Long) authentication.getCredentials();
    }
}