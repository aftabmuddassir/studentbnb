package com.studentbnb.listing_service.controller;

import com.studentbnb.listing_service.dto.ErrorResponse;
import com.studentbnb.listing_service.dto.SuccessResponse;
import com.studentbnb.listing_service.entity.AmenityType;
import com.studentbnb.listing_service.entity.Listing;
import com.studentbnb.listing_service.entity.ListingAmenity;
import com.studentbnb.listing_service.repository.ListingRepository;
import com.studentbnb.listing_service.service.ListingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/listings")
public class ListingAmenityController {

    @Autowired
    private ListingRepository listingRepository;

    // Get all amenities for a listing (Public endpoint)
    @GetMapping("/{listingId}/amenities")
    public ResponseEntity<?> getListingAmenities(@PathVariable Long listingId) {
        try {
            Optional<Listing> listingOpt = listingRepository.findById(listingId);

            if (listingOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Not Found", "Listing not found", 404, "/api/listings/" + listingId + "/amenities"));
            }

            Listing listing = listingOpt.get();
            List<AmenityType> amenities = listing.getAmenities().stream()
                    .map(ListingAmenity::getAmenityType)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(amenities);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to fetch amenities: " + e.getMessage(), 500, "/api/listings/" + listingId + "/amenities"));
        }
    }

    // Add amenity to listing (Landlords and Students only)
    @PostMapping("/{listingId}/amenities")
    public ResponseEntity<?> addAmenity(@PathVariable Long listingId, @RequestBody Map<String, String> request) {
        try {
            Long userId = getCurrentUserId();
            String amenityTypeStr = request.get("amenityType");

            if (amenityTypeStr == null || amenityTypeStr.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Bad Request", "amenityType is required", 400, "/api/listings/" + listingId + "/amenities"));
            }

            // Validate amenity type
            AmenityType amenityType;
            try {
                amenityType = AmenityType.valueOf(amenityTypeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Bad Request", "Invalid amenity type: " + amenityTypeStr, 400, "/api/listings/" + listingId + "/amenities"));
            }

            Optional<Listing> listingOpt = listingRepository.findById(listingId);

            if (listingOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Not Found", "Listing not found", 404, "/api/listings/" + listingId + "/amenities"));
            }

            Listing listing = listingOpt.get();

            // Verify ownership
            if (!listing.getLandlordId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("Forbidden", "You can only add amenities to your own listings", 403, "/api/listings/" + listingId + "/amenities"));
            }

            // Check if amenity already exists
            boolean exists = listing.getAmenities().stream()
                    .anyMatch(a -> a.getAmenityType() == amenityType);

            if (exists) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Bad Request", "Amenity already exists", 400, "/api/listings/" + listingId + "/amenities"));
            }

            // Add amenity
            ListingAmenity amenity = new ListingAmenity();
            amenity.setListing(listing);
            amenity.setAmenityType(amenityType);
            listing.getAmenities().add(amenity);

            listingRepository.save(listing);

            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse("Amenity added successfully", amenityType));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to add amenity: " + e.getMessage(), 500, "/api/listings/" + listingId + "/amenities"));
        }
    }

    // Add multiple amenities to listing (Landlords and Students only)
    @PostMapping("/{listingId}/amenities/bulk")
    public ResponseEntity<?> addMultipleAmenities(@PathVariable Long listingId, @RequestBody Map<String, List<String>> request) {
        try {
            Long userId = getCurrentUserId();
            List<String> amenityTypeStrs = request.get("amenityTypes");

            if (amenityTypeStrs == null || amenityTypeStrs.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Bad Request", "amenityTypes array is required", 400, "/api/listings/" + listingId + "/amenities/bulk"));
            }

            Optional<Listing> listingOpt = listingRepository.findById(listingId);

            if (listingOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Not Found", "Listing not found", 404, "/api/listings/" + listingId + "/amenities/bulk"));
            }

            Listing listing = listingOpt.get();

            // Verify ownership
            if (!listing.getLandlordId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("Forbidden", "You can only add amenities to your own listings", 403, "/api/listings/" + listingId + "/amenities/bulk"));
            }

            // Add all amenities
            for (String amenityTypeStr : amenityTypeStrs) {
                try {
                    AmenityType amenityType = AmenityType.valueOf(amenityTypeStr.toUpperCase());

                    // Check if amenity already exists
                    boolean exists = listing.getAmenities().stream()
                            .anyMatch(a -> a.getAmenityType() == amenityType);

                    if (!exists) {
                        ListingAmenity amenity = new ListingAmenity();
                        amenity.setListing(listing);
                        amenity.setAmenityType(amenityType);
                        listing.getAmenities().add(amenity);
                    }
                } catch (IllegalArgumentException e) {
                    // Skip invalid amenity types
                }
            }

            listingRepository.save(listing);

            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse("Amenities added successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to add amenities: " + e.getMessage(), 500, "/api/listings/" + listingId + "/amenities/bulk"));
        }
    }

    // Delete amenity from listing (Landlords and Students only)
    @DeleteMapping("/{listingId}/amenities/{amenityType}")
    public ResponseEntity<?> deleteAmenity(@PathVariable Long listingId, @PathVariable String amenityType) {
        try {
            Long userId = getCurrentUserId();

            // Validate amenity type
            AmenityType amenityTypeEnum;
            try {
                amenityTypeEnum = AmenityType.valueOf(amenityType.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Bad Request", "Invalid amenity type: " + amenityType, 400, "/api/listings/" + listingId + "/amenities/" + amenityType));
            }

            Optional<Listing> listingOpt = listingRepository.findById(listingId);

            if (listingOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Not Found", "Listing not found", 404, "/api/listings/" + listingId + "/amenities/" + amenityType));
            }

            Listing listing = listingOpt.get();

            // Verify ownership
            if (!listing.getLandlordId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("Forbidden", "You can only delete amenities from your own listings", 403, "/api/listings/" + listingId + "/amenities/" + amenityType));
            }

            // Remove amenity
            boolean removed = listing.getAmenities().removeIf(a -> a.getAmenityType() == amenityTypeEnum);

            if (!removed) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Not Found", "Amenity not found in this listing", 404, "/api/listings/" + listingId + "/amenities/" + amenityType));
            }

            listingRepository.save(listing);

            return ResponseEntity.ok(new SuccessResponse("Amenity deleted successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to delete amenity: " + e.getMessage(), 500, "/api/listings/" + listingId + "/amenities/" + amenityType));
        }
    }

    // Get all available amenity types (Public endpoint)
    @GetMapping("/amenities/types")
    public ResponseEntity<?> getAllAmenityTypes() {
        try {
            AmenityType[] amenityTypes = AmenityType.values();
            return ResponseEntity.ok(amenityTypes);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to fetch amenity types: " + e.getMessage(), 500, "/api/listings/amenities/types"));
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
