package com.studentbnb.listing_service.controller;


import com.studentbnb.listing_service.dto.*;
import com.studentbnb.listing_service.entity.ListingStatus;
import com.studentbnb.listing_service.service.ListingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/listings")
public class ListingController {

    @Autowired
    private ListingService listingService;

    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
            "status", "Listing Service is running",
            "service", "listing-service",
            "version", "1.0.0"
        ));
    }

    // Create new listing (Landlords only)
    @PostMapping
    public ResponseEntity<?> createListing(@Valid @RequestBody CreateListingRequest request) {
        try {
            Long landlordId = getCurrentUserId();
            ListingResponse listing = listingService.createListing(landlordId, request);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse("Listing created successfully", listing));
                
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Bad Request", e.getMessage(), 400, "/api/listings"));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to create listing: " + e.getMessage(), 500, "/api/listings"));
        }
    }

    // Get listing by ID (Public endpoint with view tracking)
    @GetMapping("/{id}")
    public ResponseEntity<?> getListingById(@PathVariable Long id, HttpServletRequest request) {
        try {
            // Get user info if authenticated (optional for public endpoint)
            Long userId = getCurrentUserIdOptional();
            String ipAddress = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");
            
            Optional<ListingResponse> listing = listingService.getListingByIdWithView(id, userId, ipAddress, userAgent);
            
            if (listing.isPresent()) {
                return ResponseEntity.ok(listing.get());
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to fetch listing: " + e.getMessage(), 500, "/api/listings/" + id));
        }
    }

    // Get all active listings (Public endpoint)
    @GetMapping
    public ResponseEntity<?> getActiveListings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Page<ListingResponse> listings = listingService.getActiveListings(page, size, sortBy, sortDir);
            return ResponseEntity.ok(listings);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to fetch listings: " + e.getMessage(), 500, "/api/listings"));
        }
    }

    // Search listings (Public endpoint)
    @PostMapping("/search")
    public ResponseEntity<?> searchListings(
            @RequestBody ListingSearchRequest searchRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<ListingResponse> listings = listingService.searchListings(searchRequest, page, size);
            return ResponseEntity.ok(listings);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to search listings: " + e.getMessage(), 500, "/api/listings/search"));
        }
    }

    // Get listings near campus (Public endpoint)
    @GetMapping("/nearby")
    public ResponseEntity<?> getListingsNearCampus(@RequestParam Double maxDistance) {
        try {
            List<ListingResponse> listings = listingService.getListingsNearCampus(maxDistance);
            return ResponseEntity.ok(listings);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to fetch nearby listings: " + e.getMessage(), 500, "/api/listings/nearby"));
        }
    }

    // Get listings by university (Public endpoint)
    @GetMapping("/university/{universityName}")
    public ResponseEntity<?> getListingsByUniversity(@PathVariable String universityName) {
        try {
            List<ListingResponse> listings = listingService.getListingsByUniversity(universityName);
            return ResponseEntity.ok(listings);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to fetch listings by university: " + e.getMessage(), 500, "/api/listings/university/" + universityName));
        }
    }

    // Get recent listings (Public endpoint)
    @GetMapping("/recent")
    public ResponseEntity<?> getRecentListings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<ListingResponse> listings = listingService.getRecentListings(page, size);
            return ResponseEntity.ok(listings);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to fetch recent listings: " + e.getMessage(), 500, "/api/listings/recent"));
        }
    }

    // Get popular listings (Public endpoint)
    @GetMapping("/popular")
    public ResponseEntity<?> getPopularListings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<ListingResponse> listings = listingService.getPopularListings(page, size);
            return ResponseEntity.ok(listings);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to fetch popular listings: " + e.getMessage(), 500, "/api/listings/popular"));
        }
    }

    // Update listing (Landlords only)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateListing(@PathVariable Long id, @Valid @RequestBody UpdateListingRequest request) {
        try {
            Long landlordId = getCurrentUserId();
            ListingResponse listing = listingService.updateListing(id, landlordId, request);
            
            return ResponseEntity.ok(new SuccessResponse("Listing updated successfully", listing));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Bad Request", e.getMessage(), 400, "/api/listings/" + id));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to update listing: " + e.getMessage(), 500, "/api/listings/" + id));
        }
    }

    // Change listing status (Landlords only)
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> changeListingStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            Long landlordId = getCurrentUserId();
            String statusStr = request.get("status");
            
            if (statusStr == null) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Bad Request", "Status is required", 400, "/api/listings/" + id + "/status"));
            }
            
            ListingStatus status = ListingStatus.valueOf(statusStr.toUpperCase());
            ListingResponse listing = listingService.changeListingStatus(id, landlordId, status);
            
            return ResponseEntity.ok(new SuccessResponse("Listing status updated successfully", listing));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Bad Request", e.getMessage(), 400, "/api/listings/" + id + "/status"));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to update listing status: " + e.getMessage(), 500, "/api/listings/" + id + "/status"));
        }
    }

    // Delete listing (Landlords only)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteListing(@PathVariable Long id) {
        try {
            Long landlordId = getCurrentUserId();
            listingService.deleteListing(id, landlordId);
            
            return ResponseEntity.ok(new SuccessResponse("Listing deleted successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Bad Request", e.getMessage(), 400, "/api/listings/" + id));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to delete listing: " + e.getMessage(), 500, "/api/listings/" + id));
        }
    }

    // Get landlord's own listings (Landlords only)
    @GetMapping("/my-listings")
    public ResponseEntity<?> getMyListings() {
        try {
            Long landlordId = getCurrentUserId();
            List<ListingResponse> listings = listingService.getLandlordListings(landlordId);
            
            return ResponseEntity.ok(listings);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to fetch your listings: " + e.getMessage(), 500, "/api/listings/my-listings"));
        }
    }

    // Helper methods
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getCredentials() == null) {
            throw new IllegalStateException("User not authenticated");
        }
        return (Long) authentication.getCredentials();
    }

    private Long getCurrentUserIdOptional() {
        try {
            return getCurrentUserId();
        } catch (IllegalStateException e) {
            return null; // User not authenticated, which is okay for public endpoints
        }
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("User not authenticated");
        }
        return authentication.getName();
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}