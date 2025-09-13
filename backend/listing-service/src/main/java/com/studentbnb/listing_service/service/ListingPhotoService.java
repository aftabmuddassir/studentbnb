package com.studentbnb.listing_service.service;


import com.studentbnb.listing_service.dto.AddPhotoRequest;
import com.studentbnb.listing_service.entity.Listing;
import com.studentbnb.listing_service.entity.ListingPhoto;
import com.studentbnb.listing_service.repository.ListingPhotoRepository;
import com.studentbnb.listing_service.repository.ListingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ListingPhotoService {

    @Autowired
    private ListingPhotoRepository photoRepository;
    
    @Autowired
    private ListingRepository listingRepository;

    // Add photo to listing
    @Transactional
    public ListingPhoto addPhoto(Long listingId, Long landlordId, AddPhotoRequest request) {
        Listing listing = listingRepository.findById(listingId)
            .orElseThrow(() -> new IllegalArgumentException("Listing not found with ID: " + listingId));
        
        // Verify ownership
        if (!listing.getLandlordId().equals(landlordId)) {
            throw new IllegalArgumentException("You don't have permission to add photos to this listing");
        }
        
        // Validate photo URL
        validatePhotoUrl(request.getPhotoUrl());
        
        // Get current photo count for display order
        Long photoCount = photoRepository.countPhotosByListingId(listingId);
        
        // If this is set as primary, unset current primary
        if (request.getIsPrimary() != null && request.getIsPrimary()) {
            setPrimaryPhoto(listingId, null);
        }
        
        ListingPhoto photo = new ListingPhoto();
        photo.setListing(listing);
        photo.setPhotoUrl(request.getPhotoUrl());
        photo.setDescription(request.getDescription());
        photo.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : photoCount.intValue());
        photo.setIsPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : (photoCount == 0));
        
        return photoRepository.save(photo);
    }

    // Get all photos for a listing
    public List<ListingPhoto> getListingPhotos(Long listingId) {
        return photoRepository.findByListingIdOrderByDisplayOrderAsc(listingId);
    }

    // Get primary photo for a listing
    public Optional<ListingPhoto> getPrimaryPhoto(Long listingId) {
        return photoRepository.findByListingIdAndIsPrimaryTrue(listingId);
    }

    // Update photo
    @Transactional
    public ListingPhoto updatePhoto(Long photoId, Long landlordId, AddPhotoRequest request) {
        ListingPhoto photo = photoRepository.findById(photoId)
            .orElseThrow(() -> new IllegalArgumentException("Photo not found with ID: " + photoId));
        
        // Verify ownership
        if (!photo.getListing().getLandlordId().equals(landlordId)) {
            throw new IllegalArgumentException("You don't have permission to update this photo");
        }
        
        if (request.getPhotoUrl() != null) {
            validatePhotoUrl(request.getPhotoUrl());
            photo.setPhotoUrl(request.getPhotoUrl());
        }
        
        if (request.getDescription() != null) {
            photo.setDescription(request.getDescription());
        }
        
        if (request.getDisplayOrder() != null) {
            photo.setDisplayOrder(request.getDisplayOrder());
        }
        
        if (request.getIsPrimary() != null && request.getIsPrimary()) {
            setPrimaryPhoto(photo.getListing().getId(), photoId);
            photo.setIsPrimary(true);
        }
        
        return photoRepository.save(photo);
    }

    // Delete photo
    @Transactional
    public void deletePhoto(Long photoId, Long landlordId) {
        ListingPhoto photo = photoRepository.findById(photoId)
            .orElseThrow(() -> new IllegalArgumentException("Photo not found with ID: " + photoId));
        
        // Verify ownership
        if (!photo.getListing().getLandlordId().equals(landlordId)) {
            throw new IllegalArgumentException("You don't have permission to delete this photo");
        }
        
        boolean wasPrimary = photo.getIsPrimary();
        Long listingId = photo.getListing().getId();
        
        photoRepository.delete(photo);
        
        // If deleted photo was primary, set another photo as primary
        if (wasPrimary) {
            List<ListingPhoto> remainingPhotos = photoRepository.findByListingIdOrderByDisplayOrderAsc(listingId);
            if (!remainingPhotos.isEmpty()) {
                ListingPhoto newPrimary = remainingPhotos.get(0);
                newPrimary.setIsPrimary(true);
                photoRepository.save(newPrimary);
            }
        }
    }

    // Set primary photo
    @Transactional
    public void setPrimaryPhoto(Long listingId, Long photoId) {
        // Unset current primary
        List<ListingPhoto> photos = photoRepository.findByListingIdOrderByDisplayOrderAsc(listingId);
        photos.forEach(photo -> {
            if (photo.getIsPrimary()) {
                photo.setIsPrimary(false);
                photoRepository.save(photo);
            }
        });
        
        // Set new primary if photoId is provided
        if (photoId != null) {
            ListingPhoto newPrimary = photoRepository.findById(photoId)
                .orElseThrow(() -> new IllegalArgumentException("Photo not found with ID: " + photoId));
            
            if (!newPrimary.getListing().getId().equals(listingId)) {
                throw new IllegalArgumentException("Photo does not belong to this listing");
            }
            
            newPrimary.setIsPrimary(true);
            photoRepository.save(newPrimary);
        }
    }

    // Reorder photos
    @Transactional
    public void reorderPhotos(Long listingId, Long landlordId, List<Long> photoIds) {
        Listing listing = listingRepository.findById(listingId)
            .orElseThrow(() -> new IllegalArgumentException("Listing not found with ID: " + listingId));
        
        // Verify ownership
        if (!listing.getLandlordId().equals(landlordId)) {
            throw new IllegalArgumentException("You don't have permission to reorder photos for this listing");
        }
        
        for (int i = 0; i < photoIds.size(); i++) {
            Long photoId = photoIds.get(i);
            ListingPhoto photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new IllegalArgumentException("Photo not found with ID: " + photoId));
            
            if (!photo.getListing().getId().equals(listingId)) {
                throw new IllegalArgumentException("Photo does not belong to this listing");
            }
            
            photo.setDisplayOrder(i);
            photoRepository.save(photo);
        }
    }

    // Delete all photos for a listing
    @Transactional
    public void deleteAllPhotos(Long listingId, Long landlordId) {
        Listing listing = listingRepository.findById(listingId)
            .orElseThrow(() -> new IllegalArgumentException("Listing not found with ID: " + listingId));
        
        // Verify ownership
        if (!listing.getLandlordId().equals(landlordId)) {
            throw new IllegalArgumentException("You don't have permission to delete photos for this listing");
        }
        
        photoRepository.deleteByListingId(listingId);
    }

    // Helper methods
    private void validatePhotoUrl(String photoUrl) {
        if (photoUrl == null || photoUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Photo URL cannot be empty");
        }
        
        // Basic URL validation
        if (!photoUrl.startsWith("http://") && !photoUrl.startsWith("https://")) {
            throw new IllegalArgumentException("Photo URL must start with http:// or https://");
        }
        
        // Check if it's a Google Drive URL and convert to direct link if needed
        if (photoUrl.contains("drive.google.com/file/d/")) {
            // Convert to direct link format for better performance
            String fileId = extractGoogleDriveFileId(photoUrl);
            if (fileId != null) {
                // This conversion allows direct image display
                String directUrl = "https://drive.google.com/uc?id=" + fileId;
                // Note: We could update the URL here, but for now just validate
            }
        }
        
        if (photoUrl.length() > 1000) {
            throw new IllegalArgumentException("Photo URL is too long (max 1000 characters)");
        }
    }

    private String extractGoogleDriveFileId(String driveUrl) {
        try {
            // Extract file ID from Google Drive URL
            // Format: https://drive.google.com/file/d/FILE_ID/view
            String pattern = "/file/d/([a-zA-Z0-9-_]+)";
            java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher matcher = regex.matcher(driveUrl);
            
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            // If extraction fails, return null and use original URL
        }
        return null;
    }
}