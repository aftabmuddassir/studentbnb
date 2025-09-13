package com.studentbnb.listing_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "listing_amenities")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListingAmenity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "amenity_type", nullable = false)
    private AmenityType amenityType;
    
    private String description;
    
    @Column(name = "is_available")
    private Boolean isAvailable = true;
}