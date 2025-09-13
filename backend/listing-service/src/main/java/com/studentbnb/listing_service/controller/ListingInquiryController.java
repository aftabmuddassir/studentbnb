package com.studentbnb.listing_service.controller;

import com.studentbnb.listing_service.dto.*;
import com.studentbnb.listing_service.entity.InquiryStatus;
import com.studentbnb.listing_service.service.ListingInquiryService;
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
public class ListingInquiryController {

    @Autowired
    private ListingInquiryService inquiryService;

    // Create inquiry for a listing (Students only)
    @PostMapping("/{listingId}/inquiry")
    public ResponseEntity<?> createInquiry(@PathVariable Long listingId, @Valid @RequestBody CreateInquiryRequest request) {
        try {
            Long studentId = getCurrentUserId();
            InquiryResponse inquiry = inquiryService.createInquiry(listingId, studentId, request);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse("Inquiry sent successfully", inquiry));
                
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Bad Request", e.getMessage(), 400, "/api/listings/" + listingId + "/inquiry"));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to create inquiry: " + e.getMessage(), 500, "/api/listings/" + listingId + "/inquiry"));
        }
    }

    // Get inquiry by ID (Both landlords and students)
    @GetMapping("/inquiries/{inquiryId}")
    public ResponseEntity<?> getInquiryById(@PathVariable Long inquiryId) {
        try {
            Optional<InquiryResponse> inquiry = inquiryService.getInquiryById(inquiryId);
            
            if (inquiry.isPresent()) {
                // Verify user has permission to view this inquiry
                Long currentUserId = getCurrentUserId();
                InquiryResponse inquiryData = inquiry.get();
                
                if (!inquiryData.getStudentId().equals(currentUserId) && 
                    !inquiryData.getLandlordId().equals(currentUserId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Forbidden", "You don't have permission to view this inquiry", 403, "/api/listings/inquiries/" + inquiryId));
                }
                
                return ResponseEntity.ok(inquiryData);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to fetch inquiry: " + e.getMessage(), 500, "/api/listings/inquiries/" + inquiryId));
        }
    }

    // Get inquiries for a specific listing (Landlords only)
    @GetMapping("/{listingId}/inquiries")
    public ResponseEntity<?> getListingInquiries(
            @PathVariable Long listingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Long landlordId = getCurrentUserId();
            Page<InquiryResponse> inquiries = inquiryService.getListingInquiries(listingId, landlordId, page, size);
            
            return ResponseEntity.ok(inquiries);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Bad Request", e.getMessage(), 400, "/api/listings/" + listingId + "/inquiries"));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to fetch listing inquiries: " + e.getMessage(), 500, "/api/listings/" + listingId + "/inquiries"));
        }
    }

    // Get all inquiries for current landlord (Landlords only)
    @GetMapping("/inquiries/landlord")
    public ResponseEntity<?> getLandlordInquiries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Long landlordId = getCurrentUserId();
            Page<InquiryResponse> inquiries = inquiryService.getLandlordInquiries(landlordId, page, size);
            
            return ResponseEntity.ok(inquiries);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to fetch landlord inquiries: " + e.getMessage(), 500, "/api/listings/inquiries/landlord"));
        }
    }

    // Get all inquiries for current student (Students only)
    @GetMapping("/inquiries/student")
    public ResponseEntity<?> getStudentInquiries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Long studentId = getCurrentUserId();
            Page<InquiryResponse> inquiries = inquiryService.getStudentInquiries(studentId, page, size);
            
            return ResponseEntity.ok(inquiries);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to fetch student inquiries: " + e.getMessage(), 500, "/api/listings/inquiries/student"));
        }
    }

    // Get pending inquiries for landlord (Landlords only)
    @GetMapping("/inquiries/landlord/pending")
    public ResponseEntity<?> getPendingInquiries() {
        try {
            Long landlordId = getCurrentUserId();
            List<InquiryResponse> inquiries = inquiryService.getPendingInquiries(landlordId);
            
            return ResponseEntity.ok(inquiries);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to fetch pending inquiries: " + e.getMessage(), 500, "/api/listings/inquiries/landlord/pending"));
        }
    }

    // Respond to inquiry (Landlords only)
    @PostMapping("/inquiries/{inquiryId}/respond")
    public ResponseEntity<?> respondToInquiry(@PathVariable Long inquiryId, @Valid @RequestBody RespondToInquiryRequest request) {
        try {
            Long landlordId = getCurrentUserId();
            InquiryResponse inquiry = inquiryService.respondToInquiry(inquiryId, landlordId, request);
            
            return ResponseEntity.ok(new SuccessResponse("Response sent successfully", inquiry));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Bad Request", e.getMessage(), 400, "/api/listings/inquiries/" + inquiryId + "/respond"));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to respond to inquiry: " + e.getMessage(), 500, "/api/listings/inquiries/" + inquiryId + "/respond"));
        }
    }

    // Update inquiry status (Landlords only)
    @PatchMapping("/inquiries/{inquiryId}/status")
    public ResponseEntity<?> updateInquiryStatus(@PathVariable Long inquiryId, @RequestBody Map<String, String> request) {
        try {
            Long landlordId = getCurrentUserId();
            String statusStr = request.get("status");
            
            if (statusStr == null) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Bad Request", "Status is required", 400, "/api/listings/inquiries/" + inquiryId + "/status"));
            }
            
            InquiryStatus status = InquiryStatus.valueOf(statusStr.toUpperCase());
            InquiryResponse inquiry = inquiryService.updateInquiryStatus(inquiryId, landlordId, status);
            
            return ResponseEntity.ok(new SuccessResponse("Inquiry status updated successfully", inquiry));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Bad Request", e.getMessage(), 400, "/api/listings/inquiries/" + inquiryId + "/status"));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to update inquiry status: " + e.getMessage(), 500, "/api/listings/inquiries/" + inquiryId + "/status"));
        }
    }

    // Archive inquiry (Both landlords and students)
    @PatchMapping("/inquiries/{inquiryId}/archive")
    public ResponseEntity<?> archiveInquiry(@PathVariable Long inquiryId) {
        try {
            Long userId = getCurrentUserId();
            inquiryService.archiveInquiry(inquiryId, userId);
            
            return ResponseEntity.ok(new SuccessResponse("Inquiry archived successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Bad Request", e.getMessage(), 400, "/api/listings/inquiries/" + inquiryId + "/archive"));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to archive inquiry: " + e.getMessage(), 500, "/api/listings/inquiries/" + inquiryId + "/archive"));
        }
    }

    // Delete inquiry (Students only - only if not responded to)
    @DeleteMapping("/inquiries/{inquiryId}")
    public ResponseEntity<?> deleteInquiry(@PathVariable Long inquiryId) {
        try {
            Long studentId = getCurrentUserId();
            inquiryService.deleteInquiry(inquiryId, studentId);
            
            return ResponseEntity.ok(new SuccessResponse("Inquiry deleted successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Bad Request", e.getMessage(), 400, "/api/listings/inquiries/" + inquiryId));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to delete inquiry: " + e.getMessage(), 500, "/api/listings/inquiries/" + inquiryId));
        }
    }

    // Get inquiry statistics for landlord (Landlords only)
    @GetMapping("/inquiries/landlord/stats")
    public ResponseEntity<?> getLandlordInquiryStats() {
        try {
            Long landlordId = getCurrentUserId();
            Map<String, Object> stats = inquiryService.getLandlordInquiryStats(landlordId);
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to fetch inquiry stats: " + e.getMessage(), 500, "/api/listings/inquiries/landlord/stats"));
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