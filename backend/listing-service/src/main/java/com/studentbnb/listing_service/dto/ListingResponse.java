package com.studentbnb.listing_service.dto;


import com.studentbnb.listing_service.entity.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class ListingResponse {
    
    private Long id;
    private Long landlordId;
    
    // Basic listing info
    private String title;
    private String description;
    private BigDecimal rent;
    private String currency;
    private BigDecimal securityDeposit;
    private Boolean utilitiesIncluded;
    
    // Property details
    private Integer bedrooms;
    private Integer bathrooms;
    private BigDecimal squareFeet;
    private PropertyType propertyType;
    
    // Location
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private Double latitude;
    private Double longitude;
    private Double distanceToCampusKm;
    private String nearestUniversity;
    
    // Lease details
    private LeaseType leaseType;
    private Integer leaseDurationMonths;
    private LocalDate availableFrom;
    private LocalDate availableUntil;
    
    // Preferences
    private Boolean petsAllowed;
    private Boolean smokingAllowed;
    private Boolean furnished;
    
    // Contact
    private String contactEmail;
    private String contactPhone;
    
    // Status and metadata
    private ListingStatus status;
    private Integer viewCount;
    private Integer favoriteCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Related data
    private List<ListingPhoto> photos;
    private List<ListingAmenity> amenities;
    
    // Computed fields
    private Boolean isFavorited; // Set based on current user
    private String primaryPhotoUrl;
    
    // Helper method to get primary photo URL
    public String getPrimaryPhotoUrl() {
        if (photos != null && !photos.isEmpty()) {
            return photos.stream()
                    .filter(ListingPhoto::getIsPrimary)
                    .map(ListingPhoto::getPhotoUrl)
                    .findFirst()
                    .orElse(photos.get(0).getPhotoUrl());
        }
        return null;
    }
}