package com.studentbnb.listing_service.repository;

import com.studentbnb.listing_service.entity.ListingPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListingPreferenceRepository extends JpaRepository<ListingPreference, Long> {
    List<ListingPreference> findByListingId(Long listingId);
    void deleteByListingId(Long listingId);
}
