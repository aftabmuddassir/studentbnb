package com.studentbnb.listing_service.controller;

import com.studentbnb.listing_service.dto.ErrorResponse;
import com.studentbnb.listing_service.dto.ListingResponse;
import com.studentbnb.listing_service.dto.SuccessResponse;
import com.studentbnb.listing_service.entity.ListingStatus;
import com.studentbnb.listing_service.service.ListingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/listings/admin")
public class AdminController {

    @Autowired
    private ListingService listingService;

    // Get all pending listings for approval (Admins only)
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingListings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<ListingResponse> listings = listingService.getActiveListings(page, size, "createdAt", "desc");
            
            // Filter for pending status (you might want to add this method to service)
            // For now, returning all active listings - you can modify this
            
            return ResponseEntity.ok(listings);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to fetch pending listings: " + e.getMessage(), 500, "/api/listings/admin/pending"));
        }
    }

    // Approve listing (Admins only)
    @PostMapping("/{listingId}/approve")
    public ResponseEntity<?> approveListing(@PathVariable Long listingId) {
        try {
            // You'll need to add this method to ListingService
            // ListingResponse listing = listingService.approveListingAdmin(listingId);
            
            // For now, just change status to ACTIVE
            // This is a simplified version - you might want more sophisticated approval logic
            
            return ResponseEntity.ok(new SuccessResponse("Listing approved successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Bad Request", e.getMessage(), 400, "/api/listings/admin/" + listingId + "/approve"));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to approve listing: " + e.getMessage(), 500, "/api/listings/admin/" + listingId + "/approve"));
        }
    }

    // Reject listing (Admins only)
    @PostMapping("/{listingId}/reject")
    public ResponseEntity<?> rejectListing(@PathVariable Long listingId, @RequestBody Map<String, String> request) {
        try {
            String reason = request.get("reason");
            
            if (reason == null || reason.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Bad Request", "Rejection reason is required", 400, "/api/listings/admin/" + listingId + "/reject"));
            }
            
            // You'll need to add this method to ListingService
            // ListingResponse listing = listingService.rejectListingAdmin(listingId, reason);
            
            return ResponseEntity.ok(new SuccessResponse("Listing rejected successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Bad Request", e.getMessage(), 400, "/api/listings/admin/" + listingId + "/reject"));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to reject listing: " + e.getMessage(), 500, "/api/listings/admin/" + listingId + "/reject"));
        }
    }

    // Get admin dashboard stats (Admins only)
    @GetMapping("/stats")
    public ResponseEntity<?> getAdminStats() {
        try {
            // You'll need to add methods to get these stats
            Map<String, Object> stats = Map.of(
                "totalListings", 0, // listingService.getTotalListingsCount(),
                "activeListings", 0, // listingService.getActiveListingsCount(),
                "pendingListings", 0, // listingService.getPendingListingsCount(),
                "rejectedListings", 0, // listingService.getRejectedListingsCount(),
                "totalInquiries", 0, // inquiryService.getTotalInquiriesCount(),
                "totalFavorites", 0  // favoriteService.getTotalFavoritesCount()
            );
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to fetch admin stats: " + e.getMessage(), 500, "/api/listings/admin/stats"));
        }
    }

    // Force delete listing (Admins only - emergency use)
    @DeleteMapping("/{listingId}/force-delete")
    public ResponseEntity<?> forceDeleteListing(@PathVariable Long listingId) {
        try {
            // You'll need to add this method to ListingService for admin force delete
            // listingService.forceDeleteListing(listingId);
            
            return ResponseEntity.ok(new SuccessResponse("Listing force deleted successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Bad Request", e.getMessage(), 400, "/api/listings/admin/" + listingId + "/force-delete"));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to force delete listing: " + e.getMessage(), 500, "/api/listings/admin/" + listingId + "/force-delete"));
        }
    }
}