package com.studentbnb.listing_service.controller;


import com.studentbnb.listing_service.dto.ErrorResponse;
import com.studentbnb.listing_service.service.ListingFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/listings")
public class ListingAnalyticsController {

    @Autowired
    private ListingFavoriteService favoriteService;

    // Get landlord's favorite statistics (Landlords only)
    @GetMapping("/my-listings/stats/favorites")
    public ResponseEntity<?> getLandlordFavoriteStats() {
        try {
            Long landlordId = getCurrentUserId();
            Map<String, Object> stats = favoriteService.getLandlordFavoriteStats(landlordId);
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to fetch favorite stats: " + e.getMessage(), 500, "/api/listings/my-listings/stats/favorites"));
        }
    }

    // Get favorite count for a specific listing (Public endpoint)
    @GetMapping("/{listingId}/stats/favorites")
    public ResponseEntity<?> getListingFavoriteCount(@PathVariable Long listingId) {
        try {
            Long count = favoriteService.getFavoriteCount(listingId);
            
            return ResponseEntity.ok(Map.of(
                "listingId", listingId,
                "favoriteCount", count
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to fetch favorite count: " + e.getMessage(), 500, "/api/listings/" + listingId + "/stats/favorites"));
        }
    }

    // Get landlord's overall statistics (Landlords only)
    @GetMapping("/my-listings/stats/overview")
    public ResponseEntity<?> getLandlordOverviewStats() {
        try {
            Long landlordId = getCurrentUserId();
            
            // Get favorite stats
            Map<String, Object> favoriteStats = favoriteService.getLandlordFavoriteStats(landlordId);
            
            // You can add more stats here from other services
            // Map<String, Object> inquiryStats = inquiryService.getLandlordInquiryStats(landlordId);
            // Map<String, Object> viewStats = viewService.getLandlordViewStats(landlordId);
            
            Map<String, Object> overviewStats = Map.of(
                "favorites", favoriteStats,
                "landlordId", landlordId
                // "inquiries", inquiryStats,
                // "views", viewStats
            );
            
            return ResponseEntity.ok(overviewStats);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to fetch overview stats: " + e.getMessage(), 500, "/api/listings/my-listings/stats/overview"));
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