package com.studentbnb.listing_service.repository;

import com.studentbnb.listing_service.entity.ListingFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ListingFavoriteRepository extends JpaRepository<ListingFavorite, Long> {
    
    Optional<ListingFavorite> findByListingIdAndUserId(Long listingId, Long userId);
    
    List<ListingFavorite> findByUserId(Long userId);
    
    Page<ListingFavorite> findByUserId(Long userId, Pageable pageable);
    
    List<ListingFavorite> findByListingId(Long listingId);
    
    @Query("SELECT f.listing.id FROM ListingFavorite f WHERE f.userId = :userId")
    List<Long> findListingIdsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(f) FROM ListingFavorite f WHERE f.listing.id = :listingId")
    Long countByListingId(@Param("listingId") Long listingId);
    
    @Query("SELECT COUNT(f) FROM ListingFavorite f WHERE f.userId = :userId")
    Long countByUserId(@Param("userId") Long userId);
    
    boolean existsByListingIdAndUserId(Long listingId, Long userId);
    
    void deleteByListingIdAndUserId(Long listingId, Long userId);
    
    void deleteByListingId(Long listingId);
}