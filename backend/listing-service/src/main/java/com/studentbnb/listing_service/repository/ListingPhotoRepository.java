package com.studentbnb.listing_service.repository;

import com.studentbnb.listing_service.entity.ListingPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ListingPhotoRepository extends JpaRepository<ListingPhoto, Long> {
    
    List<ListingPhoto> findByListingIdOrderByDisplayOrderAsc(Long listingId);
    
    Optional<ListingPhoto> findByListingIdAndIsPrimaryTrue(Long listingId);
    
    @Query("SELECT p FROM ListingPhoto p WHERE p.listing.id = :listingId ORDER BY p.displayOrder ASC")
    List<ListingPhoto> findPhotosByListingId(@Param("listingId") Long listingId);
    
    @Query("SELECT COUNT(p) FROM ListingPhoto p WHERE p.listing.id = :listingId")
    Long countPhotosByListingId(@Param("listingId") Long listingId);
    
    void deleteByListingId(Long listingId);
}