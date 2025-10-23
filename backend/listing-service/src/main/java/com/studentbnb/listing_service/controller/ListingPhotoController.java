package com.studentbnb.listing_service.controller;

import com.studentbnb.listing_service.dto.AddPhotoRequest;
import com.studentbnb.listing_service.dto.ErrorResponse;
import com.studentbnb.listing_service.dto.SuccessResponse;
import com.studentbnb.listing_service.entity.ListingPhoto;
import com.studentbnb.listing_service.service.CloudinaryService;
import com.studentbnb.listing_service.service.ListingPhotoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/listings")
public class ListingPhotoController {

    @Autowired
    private ListingPhotoService photoService;

    @Autowired
    private CloudinaryService cloudinaryService;

    // Upload photo file to listing (Landlords and Students) - NEW FILE UPLOAD ENDPOINT
    @PostMapping(value = "/{listingId}/photos/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadPhoto(
            @PathVariable Long listingId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "isPrimary", required = false, defaultValue = "false") Boolean isPrimary) {
        try {
            Long landlordId = getCurrentUserId();

            // Upload file to Cloudinary
            String photoUrl = cloudinaryService.uploadImage(file);

            // Create AddPhotoRequest with uploaded URL
            AddPhotoRequest request = new AddPhotoRequest();
            request.setPhotoUrl(photoUrl);
            request.setDescription(description);
            request.setIsPrimary(isPrimary);

            // Add photo to listing
            ListingPhoto photo = photoService.addPhoto(listingId, landlordId, request);

            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse("Photo uploaded successfully", photo));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Bad Request", e.getMessage(), 400, "/api/listings/" + listingId + "/photos/upload"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to upload photo: " + e.getMessage(), 500, "/api/listings/" + listingId + "/photos/upload"));
        }
    }

    // Upload multiple photos to listing (Landlords only) - NEW BULK FILE UPLOAD ENDPOINT
    @PostMapping(value = "/{listingId}/photos/upload-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadMultiplePhotos(
            @PathVariable Long listingId,
            @RequestParam("files") MultipartFile[] files) {
        try {
            Long landlordId = getCurrentUserId();

            List<ListingPhoto> uploadedPhotos = new ArrayList<>();

            // Upload all files to Cloudinary
            for (int i = 0; i < files.length; i++) {
                String photoUrl = cloudinaryService.uploadImage(files[i]);

                // Create AddPhotoRequest with uploaded URL
                AddPhotoRequest request = new AddPhotoRequest();
                request.setPhotoUrl(photoUrl);
                request.setIsPrimary(i == 0); // First photo is primary by default

                // Add photo to listing
                ListingPhoto photo = photoService.addPhoto(listingId, landlordId, request);
                uploadedPhotos.add(photo);
            }

            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse("Photos uploaded successfully", uploadedPhotos));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Bad Request", e.getMessage(), 400, "/api/listings/" + listingId + "/photos/upload-multiple"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "Failed to upload photos: " + e.getMessage(), 500, "/api/listings/" + listingId + "/photos/upload-multiple"));
        }
    }

    // Add photo to listing (Landlords only) - ORIGINAL URL-BASED ENDPOINT
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