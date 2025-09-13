package com.studentbnb.listing_service.repository;

import com.studentbnb.listing_service.entity.ListingView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ListingViewRepository extends JpaRepository<ListingView, Long> {
    
    List<ListingView> findByListingId(Long listingId);
    
    List<ListingView> findByUserId(Long userId);
    
    @Query("SELECT COUNT(v) FROM ListingView v WHERE v.listing.id = :listingId")
    Long countByListingId(@Param("listingId") Long listingId);
    
    @Query("SELECT COUNT(v) FROM ListingView v WHERE v.listing.id = :listingId AND v.viewedAt >= :since")
    Long countByListingIdSince(@Param("listingId") Long listingId, @Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(DISTINCT v.userId) FROM ListingView v WHERE v.listing.id = :listingId AND v.userId IS NOT NULL")
    Long countUniqueUsersByListingId(@Param("listingId") Long listingId);
    
    // Check if user has viewed listing recently (to avoid duplicate view counts)
    @Query("SELECT COUNT(v) FROM ListingView v WHERE v.listing.id = :listingId AND " +
           "(:userId IS NULL OR v.userId = :userId) AND " +
           "(:ipAddress IS NULL OR v.ipAddress = :ipAddress) AND " +
           "v.viewedAt >= :since")
    Long countRecentViews(@Param("listingId") Long listingId,
                         @Param("userId") Long userId,
                         @Param("ipAddress") String ipAddress,
                         @Param("since") LocalDateTime since);
}