package com.studentbnb.listing_service.service;



import com.studentbnb.listing_service.dto.CreateListingRequest;
import com.studentbnb.listing_service.dto.ListingResponse;
import com.studentbnb.listing_service.dto.ListingSearchRequest;
import com.studentbnb.listing_service.dto.UpdateListingRequest;
import com.studentbnb.listing_service.entity.*;
import com.studentbnb.listing_service.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ListingService {

    @Autowired
    private ListingRepository listingRepository;
    
    @Autowired
    private ListingPhotoRepository photoRepository;
    
    @Autowired
    private ListingAmenityRepository amenityRepository;
    
    @Autowired
    private ListingFavoriteRepository favoriteRepository;
    
    @Autowired
    private ListingViewRepository viewRepository;

    // Create new listing
    @Transactional
    public ListingResponse createListing(Long landlordId, CreateListingRequest request) {
        validateCreateListingRequest(request);
        
        Listing listing = new Listing();
        listing.setLandlordId(landlordId);
        mapRequestToListing(request, listing);
        listing.setStatus(ListingStatus.DRAFT);
        
        Listing savedListing = listingRepository.save(listing);
        
        // Add photos if provided
        if (request.getPhotoUrls() != null && !request.getPhotoUrls().isEmpty()) {
            addPhotosToListing(savedListing, request.getPhotoUrls());
        }
        
        // Add amenities if provided
        if (request.getAmenityTypes() != null && !request.getAmenityTypes().isEmpty()) {
            addAmenitiesToListing(savedListing, request.getAmenityTypes());
        }
        
        return convertToResponse(savedListing);
    }

    // Get listing by ID
    public Optional<ListingResponse> getListingById(Long id) {
        Optional<Listing> listing = listingRepository.findById(id);
        return listing.map(this::convertToResponse);
    }

    // Get listing by ID with view tracking
    @Transactional
    public Optional<ListingResponse> getListingByIdWithView(Long id, Long userId, String ipAddress, String userAgent) {
        Optional<Listing> listingOpt = listingRepository.findById(id);
        
        if (listingOpt.isPresent()) {
            Listing listing = listingOpt.get();
            
            // Track view
            trackListingView(listing, userId, ipAddress, userAgent);
            
            return Optional.of(convertToResponse(listing));
        }
        
        return Optional.empty();
    }

    // Update listing
    @Transactional
    public ListingResponse updateListing(Long listingId, Long landlordId, UpdateListingRequest request) {
        Listing listing = listingRepository.findById(listingId)
            .orElseThrow(() -> new IllegalArgumentException("Listing not found with ID: " + listingId));
        
        // Verify ownership
        if (!listing.getLandlordId().equals(landlordId)) {
            throw new IllegalArgumentException("You don't have permission to update this listing");
        }
        
        mapUpdateRequestToListing(request, listing);
        Listing savedListing = listingRepository.save(listing);
        
        return convertToResponse(savedListing);
    }

    // Delete listing
    @Transactional
    public void deleteListing(Long listingId, Long landlordId) {
        Listing listing = listingRepository.findById(listingId)
            .orElseThrow(() -> new IllegalArgumentException("Listing not found with ID: " + listingId));
        
        // Verify ownership
        if (!listing.getLandlordId().equals(landlordId)) {
            throw new IllegalArgumentException("You don't have permission to delete this listing");
        }
        
        listingRepository.delete(listing);
    }

    // Get landlord's listings
    public List<ListingResponse> getLandlordListings(Long landlordId) {
        List<Listing> listings = listingRepository.findByLandlordId(landlordId);
        return listings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Get active listings with pagination
    public Page<ListingResponse> getActiveListings(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Listing> listings = listingRepository.findActiveListings(pageable);
        
        return listings.map(this::convertToResponse);
    }

    // Search listings
    public Page<ListingResponse> searchListings(ListingSearchRequest searchRequest, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        Page<Listing> listings = listingRepository.searchListings(
            searchRequest.getCity(),
            searchRequest.getPropertyType(),
            searchRequest.getMinRent(),
            searchRequest.getMaxRent(),
            searchRequest.getMinBedrooms(),
            searchRequest.getMaxBedrooms(),
            searchRequest.getPetsAllowed(),
            searchRequest.getFurnished(),
            pageable
        );
        
        return listings.map(this::convertToResponse);
    }

    // Get listings near campus
    public List<ListingResponse> getListingsNearCampus(Double maxDistance) {
        List<Listing> listings = listingRepository.findNearCampus(maxDistance);
        return listings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Get listings by university
    public List<ListingResponse> getListingsByUniversity(String university) {
        List<Listing> listings = listingRepository.findByUniversity(university);
        return listings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Change listing status
    @Transactional
    public ListingResponse changeListingStatus(Long listingId, Long landlordId, ListingStatus status) {
        Listing listing = listingRepository.findById(listingId)
            .orElseThrow(() -> new IllegalArgumentException("Listing not found with ID: " + listingId));
        
        // Verify ownership
        if (!listing.getLandlordId().equals(landlordId)) {
            throw new IllegalArgumentException("You don't have permission to update this listing");
        }
        
        listing.setStatus(status);
        Listing savedListing = listingRepository.save(listing);
        
        return convertToResponse(savedListing);
    }

    // Get recent listings
    public Page<ListingResponse> getRecentListings(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Listing> listings = listingRepository.findRecentListings(pageable);
        return listings.map(this::convertToResponse);
    }

    // Get popular listings
    public Page<ListingResponse> getPopularListings(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Listing> listings = listingRepository.findMostViewed(pageable);
        return listings.map(this::convertToResponse);
    }

    // Helper methods
    private void validateCreateListingRequest(CreateListingRequest request) {
        if (request.getTitle() == null || request.getTitle().trim().length() < 10) {
            throw new IllegalArgumentException("Title must be at least 10 characters long");
        }
        
        if (request.getDescription() == null || request.getDescription().trim().length() < 50) {
            throw new IllegalArgumentException("Description must be at least 50 characters long");
        }
        
        if (request.getRent() == null || request.getRent().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Rent must be greater than 0");
        }
    }

    private void mapRequestToListing(CreateListingRequest request, Listing listing) {
        listing.setTitle(request.getTitle());
        listing.setDescription(request.getDescription());
        listing.setRent(request.getRent());
        listing.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
        listing.setSecurityDeposit(request.getSecurityDeposit());
        listing.setUtilitiesIncluded(request.getUtilitiesIncluded());
        listing.setBedrooms(request.getBedrooms());
        listing.setBathrooms(request.getBathrooms());
        listing.setSquareFeet(request.getSquareFeet());
        listing.setPropertyType(request.getPropertyType());
        listing.setAddress(request.getAddress());
        listing.setCity(request.getCity());
        listing.setState(request.getState());
        listing.setZipCode(request.getZipCode());
        listing.setLatitude(request.getLatitude());
        listing.setLongitude(request.getLongitude());
        listing.setDistanceToCampusKm(request.getDistanceToCampusKm());
        listing.setNearestUniversity(request.getNearestUniversity());
        listing.setLeaseType(request.getLeaseType());
        listing.setLeaseDurationMonths(request.getLeaseDurationMonths());
        listing.setAvailableFrom(request.getAvailableFrom());
        listing.setAvailableUntil(request.getAvailableUntil());
        listing.setPetsAllowed(request.getPetsAllowed());
        listing.setSmokingAllowed(request.getSmokingAllowed());
        listing.setFurnished(request.getFurnished());
        listing.setContactEmail(request.getContactEmail());
        listing.setContactPhone(request.getContactPhone());
    }

    private void mapUpdateRequestToListing(UpdateListingRequest request, Listing listing) {
        if (request.getTitle() != null) listing.setTitle(request.getTitle());
        if (request.getDescription() != null) listing.setDescription(request.getDescription());
        if (request.getRent() != null) listing.setRent(request.getRent());
        if (request.getSecurityDeposit() != null) listing.setSecurityDeposit(request.getSecurityDeposit());
        if (request.getUtilitiesIncluded() != null) listing.setUtilitiesIncluded(request.getUtilitiesIncluded());
        if (request.getAvailableFrom() != null) listing.setAvailableFrom(request.getAvailableFrom());
        if (request.getAvailableUntil() != null) listing.setAvailableUntil(request.getAvailableUntil());
        if (request.getPetsAllowed() != null) listing.setPetsAllowed(request.getPetsAllowed());
        if (request.getSmokingAllowed() != null) listing.setSmokingAllowed(request.getSmokingAllowed());
        if (request.getFurnished() != null) listing.setFurnished(request.getFurnished());
        if (request.getContactEmail() != null) listing.setContactEmail(request.getContactEmail());
        if (request.getContactPhone() != null) listing.setContactPhone(request.getContactPhone());
    }

    private void addPhotosToListing(Listing listing, List<String> photoUrls) {
        for (int i = 0; i < photoUrls.size(); i++) {
            ListingPhoto photo = new ListingPhoto();
            photo.setListing(listing);
            photo.setPhotoUrl(photoUrls.get(i));
            photo.setDisplayOrder(i);
            photo.setIsPrimary(i == 0); // First photo is primary
            photoRepository.save(photo);
        }
    }

    private void addAmenitiesToListing(Listing listing, List<AmenityType> amenityTypes) {
        for (AmenityType amenityType : amenityTypes) {
            ListingAmenity amenity = new ListingAmenity();
            amenity.setListing(listing);
            amenity.setAmenityType(amenityType);
            amenity.setIsAvailable(true);
            amenityRepository.save(amenity);
        }
    }

    private void trackListingView(Listing listing, Long userId, String ipAddress, String userAgent) {
        // Check if view was already recorded recently (prevent spam)
        Long recentViews = viewRepository.countRecentViews(
            listing.getId(), 
            userId, 
            ipAddress, 
            java.time.LocalDateTime.now().minusHours(1)
        );
        
        if (recentViews == 0) {
            ListingView view = new ListingView();
            view.setListing(listing);
            view.setUserId(userId);
            view.setIpAddress(ipAddress);
            view.setUserAgent(userAgent);
            viewRepository.save(view);
            
            // Update view count
            listing.setViewCount(listing.getViewCount() + 1);
            listingRepository.save(listing);
        }
    }

    private ListingResponse convertToResponse(Listing listing) {
        ListingResponse response = new ListingResponse();
        
        // Basic listing info
        response.setId(listing.getId());
        response.setLandlordId(listing.getLandlordId());
        response.setTitle(listing.getTitle());
        response.setDescription(listing.getDescription());
        response.setRent(listing.getRent());
        response.setCurrency(listing.getCurrency());
        response.setSecurityDeposit(listing.getSecurityDeposit());
        response.setUtilitiesIncluded(listing.getUtilitiesIncluded());
        
        // Property details
        response.setBedrooms(listing.getBedrooms());
        response.setBathrooms(listing.getBathrooms());
        response.setSquareFeet(listing.getSquareFeet());
        response.setPropertyType(listing.getPropertyType());
        
        // Location
        response.setAddress(listing.getAddress());
        response.setCity(listing.getCity());
        response.setState(listing.getState());
        response.setZipCode(listing.getZipCode());
        response.setLatitude(listing.getLatitude());
        response.setLongitude(listing.getLongitude());
        response.setDistanceToCampusKm(listing.getDistanceToCampusKm());
        response.setNearestUniversity(listing.getNearestUniversity());
        
        // Lease details
        response.setLeaseType(listing.getLeaseType());
        response.setLeaseDurationMonths(listing.getLeaseDurationMonths());
        response.setAvailableFrom(listing.getAvailableFrom());
        response.setAvailableUntil(listing.getAvailableUntil());
        
        // Preferences
        response.setPetsAllowed(listing.getPetsAllowed());
        response.setSmokingAllowed(listing.getSmokingAllowed());
        response.setFurnished(listing.getFurnished());
        
        // Contact
        response.setContactEmail(listing.getContactEmail());
        response.setContactPhone(listing.getContactPhone());
        
        // Metadata
        response.setStatus(listing.getStatus());
        response.setViewCount(listing.getViewCount());
        response.setFavoriteCount(listing.getFavoriteCount());
        response.setCreatedAt(listing.getCreatedAt());
        response.setUpdatedAt(listing.getUpdatedAt());
        
        // Load photos
        List<ListingPhoto> photos = photoRepository.findByListingIdOrderByDisplayOrderAsc(listing.getId());
        response.setPhotos(photos);
        
        // Load amenities
        List<ListingAmenity> amenities = amenityRepository.findByListingIdAndIsAvailableTrue(listing.getId());
        response.setAmenities(amenities);
        
        return response;
    }
}