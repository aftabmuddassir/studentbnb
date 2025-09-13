package com.studentbnb.listing_service.repository;



import com.studentbnb.listing_service.entity.Listing;
import com.studentbnb.listing_service.entity.ListingStatus;
import com.studentbnb.listing_service.entity.PropertyType;
import com.studentbnb.listing_service.entity.LeaseType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {
    
    // Find by landlord
    List<Listing> findByLandlordId(Long landlordId);
    
    List<Listing> findByLandlordIdAndStatus(Long landlordId, ListingStatus status);
    
    // Find by status
    List<Listing> findByStatus(ListingStatus status);
    
    Page<Listing> findByStatus(ListingStatus status, Pageable pageable);
    
    // Find active listings
    @Query("SELECT l FROM Listing l WHERE l.status = 'ACTIVE'")
    Page<Listing> findActiveListings(Pageable pageable);
    
    // Search by location
    List<Listing> findByCity(String city);
    
    List<Listing> findByCityAndStatus(String city, ListingStatus status);
    
    List<Listing> findByStateAndStatus(String state, ListingStatus status);
    
    List<Listing> findByZipCodeAndStatus(String zipCode, ListingStatus status);
    
    // Search by property details
    List<Listing> findByPropertyTypeAndStatus(PropertyType propertyType, ListingStatus status);
    
    List<Listing> findByBedroomsAndStatus(Integer bedrooms, ListingStatus status);
    
    // Search by lease type
    List<Listing> findByLeaseTypeAndStatus(LeaseType leaseType, ListingStatus status);
    
    // Price range search
    @Query("SELECT l FROM Listing l WHERE l.status = :status AND l.rent BETWEEN :minRent AND :maxRent")
    Page<Listing> findByRentRange(@Param("status") ListingStatus status, 
                                  @Param("minRent") BigDecimal minRent, 
                                  @Param("maxRent") BigDecimal maxRent, 
                                  Pageable pageable);
    
    // Availability search
    @Query("SELECT l FROM Listing l WHERE l.status = 'ACTIVE' AND " +
           "(l.availableFrom IS NULL OR l.availableFrom <= :date) AND " +
           "(l.availableUntil IS NULL OR l.availableUntil >= :date)")
    List<Listing> findAvailableOnDate(@Param("date") LocalDate date);
    
  // Advanced search
@Query("SELECT l FROM Listing l WHERE " +
       "l.status = 'ACTIVE' AND " +
       "(:city IS NULL OR LOWER(l.city) LIKE LOWER(CONCAT('%', :city, '%'))) AND " +
       "(:propertyType IS NULL OR l.propertyType = :propertyType) AND " +
       "(:minRent IS NULL OR l.rent >= :minRent) AND " +
       "(:maxRent IS NULL OR l.rent <= :maxRent) AND " +
       "(:minBedrooms IS NULL OR l.bedrooms >= :minBedrooms) AND " +
       "(:maxBedrooms IS NULL OR l.bedrooms <= :maxBedrooms) AND " +
       "(:petsAllowed IS NULL OR l.petsAllowed = :petsAllowed) AND " +
       "(:furnished IS NULL OR l.furnished = :furnished)")
Page<Listing> searchListings(@Param("city") String city,
                             @Param("propertyType") PropertyType propertyType,
                             @Param("minRent") BigDecimal minRent,
                             @Param("maxRent") BigDecimal maxRent,
                             @Param("minBedrooms") Integer minBedrooms,
                             @Param("maxBedrooms") Integer maxBedrooms,
                             @Param("petsAllowed") Boolean petsAllowed,
                             @Param("furnished") Boolean furnished,
                             Pageable pageable);
    
    // Distance-based search (for campus proximity)
    @Query("SELECT l FROM Listing l WHERE l.status = 'ACTIVE' AND " +
           "l.distanceToCampusKm <= :maxDistance ORDER BY l.distanceToCampusKm ASC")
    List<Listing> findNearCampus(@Param("maxDistance") Double maxDistance);
    
    // University-specific search
    @Query("SELECT l FROM Listing l WHERE l.status = 'ACTIVE' AND " +
           "LOWER(l.nearestUniversity) LIKE LOWER(CONCAT('%', :university, '%'))")
    List<Listing> findByUniversity(@Param("university") String university);
    
    // Popular listings (most viewed/favorited)
    @Query("SELECT l FROM Listing l WHERE l.status = 'ACTIVE' ORDER BY l.viewCount DESC")
    Page<Listing> findMostViewed(Pageable pageable);
    
    @Query("SELECT l FROM Listing l WHERE l.status = 'ACTIVE' ORDER BY l.favoriteCount DESC")
    Page<Listing> findMostFavorited(Pageable pageable);
    
    // Recent listings
    @Query("SELECT l FROM Listing l WHERE l.status = 'ACTIVE' ORDER BY l.createdAt DESC")
    Page<Listing> findRecentListings(Pageable pageable);
    
    // Statistics queries
    @Query("SELECT COUNT(l) FROM Listing l WHERE l.landlordId = :landlordId AND l.status = 'ACTIVE'")
    Long countActiveListingsByLandlord(@Param("landlordId") Long landlordId);
    
    @Query("SELECT COUNT(l) FROM Listing l WHERE l.status = :status")
    Long countByStatus(@Param("status") ListingStatus status);
}