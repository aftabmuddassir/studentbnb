package com.studentbnb.listing_service.controller;

import com.studentbnb.listing_service.dto.AddPhotoRequest;
import com.studentbnb.listing_service.dto.ErrorResponse;
import com.studentbnb.listing_service.dto.SuccessResponse;
import com.studentbnb.listing_service.entity.ListingPhoto;
import com.studentbnb.listing_service.service.ListingPhotoService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/listings")
public class ListingPhotoController {

    @Autowired
    private ListingPhotoService photoService;

    // Add photo to listing (Landlords only)
    @PostMapping("/{listingId}/photos")
    public ResponseEntity<?> addPhoto(@PathVariable Long listingId, @Valid @RequestBody AddPhotoRequest request) {
        try {
            Long landlordId = getCurrentUserId();
            ListingPhoto photo = photoService.addPhoto(listingId, landlordId, request);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse("Photo added successfully", photo));
                
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Bad Request", e.getMessage(), 400, "/api/listings/" + listingId + "/photos"));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to add photo: " + e.getMessage(), 500, "/api/listings/" + listingId + "/photos"));
        }
    }

    // Get all photos for a listing (Public endpoint)
    @GetMapping("/{listingId}/photos")
    public ResponseEntity<?> getListingPhotos(@PathVariable Long listingId) {
        try {
            List<ListingPhoto> photos = photoService.getListingPhotos(listingId);
            return ResponseEntity.ok(photos);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to fetch photos: " + e.getMessage(), 500, "/api/listings/" + listingId + "/photos"));
        }
    }

    // Get primary photo for a listing (Public endpoint)
    @GetMapping("/{listingId}/photos/primary")
    public ResponseEntity<?> getPrimaryPhoto(@PathVariable Long listingId) {
        try {
            Optional<ListingPhoto> photo = photoService.getPrimaryPhoto(listingId);
            
            if (photo.isPresent()) {
                return ResponseEntity.ok(photo.get());
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to fetch primary photo: " + e.getMessage(), 500, "/api/listings/" + listingId + "/photos/primary"));
        }
    }

    // Update photo (Landlords only)
    @PutMapping("/photos/{photoId}")
    public ResponseEntity<?> updatePhoto(@PathVariable Long photoId, @Valid @RequestBody AddPhotoRequest request) {
        try {
            Long landlordId = getCurrentUserId();
            ListingPhoto photo = photoService.updatePhoto(photoId, landlordId, request);
            
            return ResponseEntity.ok(new SuccessResponse("Photo updated successfully", photo));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Bad Request", e.getMessage(), 400, "/api/listings/photos/" + photoId));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to update photo: " + e.getMessage(), 500, "/api/listings/photos/" + photoId));
        }
    }

    // Delete photo (Landlords only)
    @DeleteMapping("/photos/{photoId}")
    public ResponseEntity<?> deletePhoto(@PathVariable Long photoId) {
        try {
            Long landlordId = getCurrentUserId();
            photoService.deletePhoto(photoId, landlordId);
            
            return ResponseEntity.ok(new SuccessResponse("Photo deleted successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Bad Request", e.getMessage(), 400, "/api/listings/photos/" + photoId));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to delete photo: " + e.getMessage(), 500, "/api/listings/photos/" + photoId));
        }
    }

    // Set primary photo (Landlords only)
    @PatchMapping("/{listingId}/photos/{photoId}/primary")
    public ResponseEntity<?> setPrimaryPhoto(@PathVariable Long listingId, @PathVariable Long photoId) {
        try {
            Long landlordId = getCurrentUserId();
            // We need to verify ownership through the photo service
            photoService.setPrimaryPhoto(listingId, photoId);
            
            return ResponseEntity.ok(new SuccessResponse("Primary photo set successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Bad Request", e.getMessage(), 400, "/api/listings/" + listingId + "/photos/" + photoId + "/primary"));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to set primary photo: " + e.getMessage(), 500, "/api/listings/" + listingId + "/photos/" + photoId + "/primary"));
        }
    }

    // Reorder photos (Landlords only)
    @PutMapping("/{listingId}/photos/reorder")
    public ResponseEntity<?> reorderPhotos(@PathVariable Long listingId, @RequestBody Map<String, List<Long>> request) {
        try {
            Long landlordId = getCurrentUserId();
            List<Long> photoIds = request.get("photoIds");
            
            if (photoIds == null || photoIds.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Bad Request", "Photo IDs are required", 400, "/api/listings/" + listingId + "/photos/reorder"));
            }
            
            photoService.reorderPhotos(listingId, landlordId, photoIds);
            
            return ResponseEntity.ok(new SuccessResponse("Photos reordered successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Bad Request", e.getMessage(), 400, "/api/listings/" + listingId + "/photos/reorder"));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to reorder photos: " + e.getMessage(), 500, "/api/listings/" + listingId + "/photos/reorder"));
        }
    }

    // Delete all photos for a listing (Landlords only)
    @DeleteMapping("/{listingId}/photos")
    public ResponseEntity<?> deleteAllPhotos(@PathVariable Long listingId) {
        try {
            Long landlordId = getCurrentUserId();
            photoService.deleteAllPhotos(listingId, landlordId);
            
            return ResponseEntity.ok(new SuccessResponse("All photos deleted successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Bad Request", e.getMessage(), 400, "/api/listings/" + listingId + "/photos"));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to delete photos: " + e.getMessage(), 500, "/api/listings/" + listingId + "/photos"));
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