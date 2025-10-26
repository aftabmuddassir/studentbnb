package com.studentbnb.listing_service.controller;

import com.studentbnb.listing_service.dto.ErrorResponse;
import com.studentbnb.listing_service.dto.SuccessResponse;
import com.studentbnb.listing_service.entity.*;
import com.studentbnb.listing_service.repository.ListingPreferenceRepository;
import com.studentbnb.listing_service.repository.ListingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/listings/{listingId}/preferences")
public class ListingPreferenceController {

    @Autowired
    private ListingPreferenceRepository preferenceRepository;

    @Autowired
    private ListingRepository listingRepository;

    // Get all preference types
    @GetMapping("/types")
    public ResponseEntity<?> getPreferenceTypes() {
        try {
            return ResponseEntity.ok(Map.of(
                "dietaryPreferences", DietaryPreference.values(),
                "genderPreferences", GenderPreference.values(),
                "smokingPreferences", SmokingPreference.values()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", e.getMessage(), 500, "/api/listings/preferences/types"));
        }
    }

    // Get listing preferences
    @GetMapping
    public ResponseEntity<?> getListingPreferences(@PathVariable Long listingId) {
        try {
            List<ListingPreference> preferences = preferenceRepository.findByListingId(listingId);
            return ResponseEntity.ok(preferences);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", e.getMessage(), 500,
                    "/api/listings/" + listingId + "/preferences"));
        }
    }

    // Add or update preferences
    @PostMapping
    public ResponseEntity<?> setPreferences(
            @PathVariable Long listingId,
            @RequestBody Map<String, String> request) {
        try {
            Long userId = getCurrentUserId();

            Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));

            if (!listing.getLandlordId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("Forbidden", "You don't have permission to modify this listing",
                        403, "/api/listings/" + listingId + "/preferences"));
            }

            // Delete existing preferences
            preferenceRepository.deleteByListingId(listingId);

            // Create new preference
            ListingPreference preference = new ListingPreference();
            preference.setListing(listing);

            // Set dietary preference
            if (request.containsKey("dietaryPreference") && request.get("dietaryPreference") != null) {
                preference.setDietaryPreference(
                    DietaryPreference.valueOf(request.get("dietaryPreference"))
                );
            }

            // Set gender preference
            if (request.containsKey("genderPreference") && request.get("genderPreference") != null) {
                preference.setGenderPreference(
                    GenderPreference.valueOf(request.get("genderPreference"))
                );
            }

            // Set smoking preference
            if (request.containsKey("smokingPreference") && request.get("smokingPreference") != null) {
                preference.setSmokingPreference(
                    SmokingPreference.valueOf(request.get("smokingPreference"))
                );
            }

            // Set additional notes
            if (request.containsKey("additionalNotes")) {
                preference.setAdditionalNotes(request.get("additionalNotes"));
            }

            preferenceRepository.save(preference);

            return ResponseEntity.ok(new SuccessResponse("Preferences updated successfully", preference));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Bad Request", e.getMessage(), 400,
                    "/api/listings/" + listingId + "/preferences"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", e.getMessage(), 500,
                    "/api/listings/" + listingId + "/preferences"));
        }
    }

    // Delete preferences
    @DeleteMapping
    public ResponseEntity<?> deletePreferences(@PathVariable Long listingId) {
        try {
            Long userId = getCurrentUserId();

            Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));

            if (!listing.getLandlordId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("Forbidden", "You don't have permission to modify this listing",
                        403, "/api/listings/" + listingId + "/preferences"));
            }

            preferenceRepository.deleteByListingId(listingId);

            return ResponseEntity.ok(new SuccessResponse("Preferences deleted successfully"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Bad Request", e.getMessage(), 400,
                    "/api/listings/" + listingId + "/preferences"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", e.getMessage(), 500,
                    "/api/listings/" + listingId + "/preferences"));
        }
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getCredentials() == null) {
            throw new IllegalStateException("User not authenticated");
        }
        return (Long) authentication.getCredentials();
    }
}
