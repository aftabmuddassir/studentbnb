package com.studentbnb.listing_service.service;

import com.studentbnb.listing_service.dto.ListingResponse;
import com.studentbnb.listing_service.entity.Listing;
import com.studentbnb.listing_service.entity.ListingFavorite;
import com.studentbnb.listing_service.entity.ListingStatus;
import com.studentbnb.listing_service.repository.ListingFavoriteRepository;
import com.studentbnb.listing_service.repository.ListingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ListingFavoriteService {

    @Autowired
    private ListingFavoriteRepository favoriteRepository;
    
    @Autowired
    private ListingRepository listingRepository;
    
    @Autowired
    private ListingService listingService;

    // Add listing to favorites
    @Transactional
    public ListingFavorite addToFavorites(Long listingId, Long userId) {
        // Check if listing exists and is active
        Listing listing = listingRepository.findById(listingId)
            .orElseThrow(() -> new IllegalArgumentException("Listing not found with ID: " + listingId));
        
        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new IllegalArgumentException("Cannot favorite an inactive listing");
        }
        
        // Check if already favorited
        if (favoriteRepository.existsByListingIdAndUserId(listingId, userId)) {
            throw new IllegalArgumentException("Listing is already in your favorites");
        }
        
        // Create favorite
        ListingFavorite favorite = new ListingFavorite();
        favorite.setListing(listing);
        favorite.setUserId(userId);
        
        ListingFavorite savedFavorite = favoriteRepository.save(favorite);
        
        // Update favorite count in listing
        updateListingFavoriteCount(listingId);
        
        return savedFavorite;
    }

    // Remove listing from favorites
    @Transactional
    public void removeFromFavorites(Long listingId, Long userId) {
        if (!favoriteRepository.existsByListingIdAndUserId(listingId, userId)) {
            throw new IllegalArgumentException("Listing is not in your favorites");
        }
        
        favoriteRepository.deleteByListingIdAndUserId(listingId, userId);
        
        // Update favorite count in listing
        updateListingFavoriteCount(listingId);
    }

    // Check if listing is favorited by user
    public boolean isListingFavorited(Long listingId, Long userId) {
        return favoriteRepository.existsByListingIdAndUserId(listingId, userId);
    }

    // Get user's favorite listings
    public List<ListingResponse> getUserFavorites(Long userId) {
        List<Long> favoriteListingIds = favoriteRepository.findListingIdsByUserId(userId);
        
        return favoriteListingIds.stream()
            .map(listingId -> listingService.getListingById(listingId))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .filter(listing -> listing.getStatus() == ListingStatus.ACTIVE) // Only return active listings
            .collect(Collectors.toList());
    }

    // Get user's favorite listings with pagination
    public Page<ListingResponse> getUserFavoritesPaginated(Long userId, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ListingFavorite> favorites = favoriteRepository.findByUserId(userId, pageable);
        
        return favorites.map(favorite -> {
            Optional<ListingResponse> listing = listingService.getListingById(favorite.getListing().getId());
            return listing.orElse(null);
        }).map(listing -> listing); // Filter out null values would be done in controller
    }

    // Get favorite count for a listing
    public Long getFavoriteCount(Long listingId) {
        return favoriteRepository.countByListingId(listingId);
    }

    // Get user's favorite count
    public Long getUserFavoriteCount(Long userId) {
        return favoriteRepository.countByUserId(userId);
    }

    // Get users who favorited a listing (for landlord insights)
    public List<ListingFavorite> getListingFavorites(Long listingId, Long landlordId) {
        // Verify ownership
        Listing listing = listingRepository.findById(listingId)
            .orElseThrow(() -> new IllegalArgumentException("Listing not found with ID: " + listingId));
        
        if (!listing.getLandlordId().equals(landlordId)) {
            throw new IllegalArgumentException("You don't have permission to view favorites for this listing");
        }
        
        return favoriteRepository.findByListingId(listingId);
    }

    // Get favorite statistics for landlord
    public Map<String, Object> getLandlordFavoriteStats(Long landlordId) {
        List<Listing> landlordListings = listingRepository.findByLandlordIdAndStatus(landlordId, ListingStatus.ACTIVE);
        
        Long totalFavorites = landlordListings.stream()
            .mapToLong(listing -> favoriteRepository.countByListingId(listing.getId()))
            .sum();
        
        // Find most favorited listing
        Optional<Listing> mostFavorited = landlordListings.stream()
            .max((l1, l2) -> {
                Long count1 = favoriteRepository.countByListingId(l1.getId());
                Long count2 = favoriteRepository.countByListingId(l2.getId());
                return count1.compareTo(count2);
            });
        
        double averageFavorites = landlordListings.isEmpty() ? 0.0 : 
            (double) totalFavorites / landlordListings.size();
        
        return Map.of(
            "totalFavorites", totalFavorites,
            "averageFavoritesPerListing", averageFavorites,
            "totalListings", landlordListings.size(),
            "mostFavoritedListingId", mostFavorited.map(Listing::getId).orElse(null),
            "mostFavoritedListingTitle", mostFavorited.map(Listing::getTitle).orElse("N/A"),
            "mostFavoritedCount", mostFavorited.map(l -> favoriteRepository.countByListingId(l.getId())).orElse(0L)
        );
    }

    // Bulk add to favorites (for user convenience)
    @Transactional
    public List<ListingFavorite> addMultipleToFavorites(List<Long> listingIds, Long userId) {
        return listingIds.stream()
            .filter(listingId -> !favoriteRepository.existsByListingIdAndUserId(listingId, userId))
            .map(listingId -> {
                try {
                    return addToFavorites(listingId, userId);
                } catch (IllegalArgumentException e) {
                    // Skip invalid listings
                    return null;
                }
            })
            .filter(favorite -> favorite != null)
            .collect(Collectors.toList());
    }

    // Bulk remove from favorites
    @Transactional
    public void removeMultipleFromFavorites(List<Long> listingIds, Long userId) {
        listingIds.forEach(listingId -> {
            try {
                removeFromFavorites(listingId, userId);
            } catch (IllegalArgumentException e) {
                // Skip if not in favorites
            }
        });
    }

    // Clear all favorites for user
    @Transactional
    public void clearAllFavorites(Long userId) {
        List<ListingFavorite> userFavorites = favoriteRepository.findByUserId(userId);
        favoriteRepository.deleteAll(userFavorites);
        
        // Update favorite counts for all affected listings
        userFavorites.forEach(favorite -> updateListingFavoriteCount(favorite.getListing().getId()));
    }

    // Helper method to update listing favorite count
    private void updateListingFavoriteCount(Long listingId) {
        Long count = favoriteRepository.countByListingId(listingId);
        
        Optional<Listing> listingOpt = listingRepository.findById(listingId);
        if (listingOpt.isPresent()) {
            Listing listing = listingOpt.get();
            listing.setFavoriteCount(count.intValue());
            listingRepository.save(listing);
        }
    }
}