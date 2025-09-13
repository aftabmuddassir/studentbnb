package com.studentbnb.listing_service.dto;



import com.studentbnb.listing_service.entity.AmenityType;
import com.studentbnb.listing_service.entity.LeaseType;
import com.studentbnb.listing_service.entity.PropertyType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
public class ListingSearchRequest {
    
    // Location filters
    private String city;
    private String state;
    private String zipCode;
    private Double latitude;
    private Double longitude;
    private Double radiusKm;
    private String nearestUniversity;
    private Double maxDistanceToCampus;
    
    // Property filters
    private PropertyType propertyType;
    private Integer minBedrooms;
    private Integer maxBedrooms;
    private Integer minBathrooms;
    private Integer maxBathrooms;
    private BigDecimal minSquareFeet;
    private BigDecimal maxSquareFeet;
    
    // Price filters
    private BigDecimal minRent;
    private BigDecimal maxRent;
    private Boolean utilitiesIncluded;
    private BigDecimal maxSecurityDeposit;
    
    // Lease filters
    private LeaseType leaseType;
    private Integer minLeaseDuration;
    private Integer maxLeaseDuration;
    private LocalDate availableFrom;
    private LocalDate availableUntil;
    
    // Preference filters
    private Boolean petsAllowed;
    private Boolean smokingAllowed;
    private Boolean furnished;
    
    // Amenity filters
    private List<AmenityType> requiredAmenities;
    private List<AmenityType> preferredAmenities;
    
    // Text search
    private String keywords; // Search in title and description
    
    // Sorting options
    private String sortBy = "createdAt"; // createdAt, rent, viewCount, favoriteCount, distanceToCampus
    private String sortDirection = "desc"; // asc, desc
}