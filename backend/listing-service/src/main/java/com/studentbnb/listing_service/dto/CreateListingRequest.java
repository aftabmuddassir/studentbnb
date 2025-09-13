package com.studentbnb.listing_service.dto;

import com.studentbnb.listing_service.entity.AmenityType;
import com.studentbnb.listing_service.entity.LeaseType;
import com.studentbnb.listing_service.entity.PropertyType;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
public class CreateListingRequest {
    
    @NotBlank(message = "Title is required")
    @Size(min = 10, max = 100, message = "Title must be between 10 and 100 characters")
    private String title;
    
    @NotBlank(message = "Description is required")
    @Size(min = 50, max = 2000, message = "Description must be between 50 and 2000 characters")
    private String description;
    
    @NotNull(message = "Rent is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Rent must be greater than 0")
    private BigDecimal rent;
    
    @Size(max = 3)
    private String currency = "USD";
    
    @NotNull(message = "Security deposit is required")
    @DecimalMin(value = "0.0", message = "Security deposit must be 0 or greater")
    private BigDecimal securityDeposit;
    
    private Boolean utilitiesIncluded = false;
    
    // Property details
    @NotNull(message = "Number of bedrooms is required")
    @Min(value = 0, message = "Bedrooms cannot be negative")
    @Max(value = 10, message = "Maximum 10 bedrooms allowed")
    private Integer bedrooms;
    
    @NotNull(message = "Number of bathrooms is required")
    @Min(value = 1, message = "At least 1 bathroom is required")
    @Max(value = 10, message = "Maximum 10 bathrooms allowed")
    private Integer bathrooms;
    
    @DecimalMin(value = "0.0", message = "Square feet must be positive")
    private BigDecimal squareFeet;
    
    @NotNull(message = "Property type is required")
    private PropertyType propertyType;
    
    // Location
    @NotBlank(message = "Address is required")
    private String address;
    
    @NotBlank(message = "City is required")
    private String city;
    
    @NotBlank(message = "State is required")
    private String state;
    
    @NotBlank(message = "ZIP code is required")
    @Pattern(regexp = "^[0-9]{5}(-[0-9]{4})?$", message = "Invalid ZIP code format")
    private String zipCode;
    
    private Double latitude;
    private Double longitude;
    private Double distanceToCampusKm;
    private String nearestUniversity;
    
    // Lease details
    @NotNull(message = "Lease type is required")
    private LeaseType leaseType;
    
    @Min(value = 1, message = "Lease duration must be at least 1 month")
    @Max(value = 24, message = "Lease duration cannot exceed 24 months")
    private Integer leaseDurationMonths;
    
    private LocalDate availableFrom;
    private LocalDate availableUntil;
    
    // Preferences
    private Boolean petsAllowed = false;
    private Boolean smokingAllowed = false;
    private Boolean furnished = false;
    
    // Contact
    @Email(message = "Invalid email format")
    private String contactEmail;
    
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Invalid phone number format")
    private String contactPhone;
    
    // Photos and amenities
    private List<String> photoUrls;
    private List<AmenityType> amenityTypes;
}