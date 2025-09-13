package com.studentbnb.listing_service.repository;


import com.studentbnb.listing_service.entity.ListingInquiry;
import com.studentbnb.listing_service.entity.InquiryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListingInquiryRepository extends JpaRepository<ListingInquiry, Long> {
    
    List<ListingInquiry> findByListingId(Long listingId);
    
    Page<ListingInquiry> findByListingId(Long listingId, Pageable pageable);
    
    List<ListingInquiry> findByStudentId(Long studentId);
    
    Page<ListingInquiry> findByStudentId(Long studentId, Pageable pageable);
    
    List<ListingInquiry> findByStatus(InquiryStatus status);
    
    @Query("SELECT i FROM ListingInquiry i WHERE i.listing.landlordId = :landlordId")
    List<ListingInquiry> findByLandlordId(@Param("landlordId") Long landlordId);
    
    @Query("SELECT i FROM ListingInquiry i WHERE i.listing.landlordId = :landlordId ORDER BY i.createdAt DESC")
    Page<ListingInquiry> findByLandlordIdOrderByCreatedAtDesc(@Param("landlordId") Long landlordId, Pageable pageable);
    
    @Query("SELECT i FROM ListingInquiry i WHERE i.listing.landlordId = :landlordId AND i.status = :status")
    List<ListingInquiry> findByLandlordIdAndStatus(@Param("landlordId") Long landlordId, 
                                                  @Param("status") InquiryStatus status);
    
    @Query("SELECT COUNT(i) FROM ListingInquiry i WHERE i.listing.id = :listingId")
    Long countByListingId(@Param("listingId") Long listingId);
    
    @Query("SELECT COUNT(i) FROM ListingInquiry i WHERE i.listing.landlordId = :landlordId AND i.status = 'PENDING'")
    Long countPendingInquiriesByLandlord(@Param("landlordId") Long landlordId);
}