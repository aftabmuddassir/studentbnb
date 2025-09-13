package com.studentbnb.listing_service.repository;


import com.studentbnb.listing_service.entity.ListingAmenity;
import com.studentbnb.listing_service.entity.AmenityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListingAmenityRepository extends JpaRepository<ListingAmenity, Long> {
    
    List<ListingAmenity> findByListingId(Long listingId);
    
    List<ListingAmenity> findByListingIdAndIsAvailableTrue(Long listingId);
    
    List<ListingAmenity> findByAmenityType(AmenityType amenityType);
    
    @Query("SELECT a FROM ListingAmenity a WHERE a.listing.id = :listingId AND a.amenityType = :amenityType")
    List<ListingAmenity> findByListingIdAndAmenityType(@Param("listingId") Long listingId, 
                                                       @Param("amenityType") AmenityType amenityType);
    
    // Find listings that have specific amenities
    @Query("SELECT DISTINCT a.listing.id FROM ListingAmenity a WHERE a.amenityType IN :amenityTypes AND a.isAvailable = true")
    List<Long> findListingIdsByAmenityTypes(@Param("amenityTypes") List<AmenityType> amenityTypes);
    
    void deleteByListingId(Long listingId);
    
    boolean existsByListingIdAndAmenityType(Long listingId, AmenityType amenityType);
}