package com.studentbnb.listing_service.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "listings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Listing {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Column(name = "landlord_id", nullable = false)
    private Long landlordId; // Reference to User ID from user-service
    
    @NotBlank
    @Size(min = 10, max = 100)
    @Column(nullable = false)
    private String title;
    
    @NotBlank
    @Size(min = 50, max = 2000)
    @Column(nullable = false, length = 2000)
    private String description;
    
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal rent;
    
    @NotBlank
    @Size(max = 3)
    private String currency = "USD";
    
    @NotNull
    @DecimalMin(value = "0.0")
    @Column(name = "security_deposit", precision = 10, scale = 2)
    private BigDecimal securityDeposit;
    
    @Column(name = "utilities_included")
    private Boolean utilitiesIncluded = false;
    
    // Property details
    @NotNull
    @Min(0)
    @Max(10)
    private Integer bedrooms;
    
    @NotNull
    @Min(1)
    @Max(10)
    private Integer bathrooms;
    
    @DecimalMin(value = "0.0")
    @Column(name = "square_feet")
    private BigDecimal squareFeet;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "property_type", nullable = false)
    private PropertyType propertyType;
    
    // Location
    @NotBlank
    @Column(nullable = false)
    private String address;
    
    @NotBlank
    @Column(nullable = false)
    private String city;
    
    @NotBlank
    @Column(nullable = false)
    private String state;
    
    @NotBlank
    @Pattern(regexp = "^[0-9]{5}(-[0-9]{4})?$")
    @Column(name = "zip_code", nullable = false)
    private String zipCode;
    
    @Column(name = "latitude")
    private Double latitude;
    
    @Column(name = "longitude")
    private Double longitude;
    
    @Column(name = "distance_to_campus_km")
    private Double distanceToCampusKm;
    
    @Column(name = "nearest_university")
    private String nearestUniversity;
    
    // Lease details
    @Enumerated(EnumType.STRING)
    @Column(name = "lease_type", nullable = false)
    private LeaseType leaseType;
    
    @Column(name = "lease_duration_months")
    private Integer leaseDurationMonths;
    
    @Column(name = "available_from")
    private LocalDate availableFrom;
    
    @Column(name = "available_until")
    private LocalDate availableUntil;
    
    // Preferences
    @Column(name = "pets_allowed")
    private Boolean petsAllowed = false;
    
    @Column(name = "smoking_allowed")
    private Boolean smokingAllowed = false;
    
    @Column(name = "furnished")
    private Boolean furnished = false;
    
    // Contact
    @Email
    @Column(name = "contact_email")
    private String contactEmail;
    
    @Pattern(regexp = "^[+]?[0-9]{10,15}$")
    @Column(name = "contact_phone")
    private String contactPhone;
    
    // Status and metadata
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ListingStatus status = ListingStatus.DRAFT;
    
    @Column(name = "view_count")
    private Integer viewCount = 0;
    
    @Column(name = "favorite_count")
    private Integer favoriteCount = 0;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ListingPhoto> photos = new ArrayList<>();

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ListingAmenity> amenities = new ArrayList<>();

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ListingPreference> preferences = new ArrayList<>();

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ListingFavorite> favorites = new ArrayList<>();
}