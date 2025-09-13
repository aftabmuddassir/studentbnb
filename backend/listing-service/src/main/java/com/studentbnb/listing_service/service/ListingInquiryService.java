package com.studentbnb.listing_service.service;


import com.studentbnb.listing_service.dto.CreateInquiryRequest;
import com.studentbnb.listing_service.dto.InquiryResponse;
import com.studentbnb.listing_service.dto.RespondToInquiryRequest;
import com.studentbnb.listing_service.entity.*;
import com.studentbnb.listing_service.repository.ListingInquiryRepository;
import com.studentbnb.listing_service.repository.ListingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ListingInquiryService {

    @Autowired
    private ListingInquiryRepository inquiryRepository;
    
    @Autowired
    private ListingRepository listingRepository;

    // Create new inquiry
    @Transactional
    public InquiryResponse createInquiry(Long listingId, Long studentId, CreateInquiryRequest request) {
        // Check if listing exists and is active
        Listing listing = listingRepository.findById(listingId)
            .orElseThrow(() -> new IllegalArgumentException("Listing not found with ID: " + listingId));
        
        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new IllegalArgumentException("Cannot inquire about an inactive listing");
        }
        
        // Validate request
        validateInquiryRequest(request);
        
        ListingInquiry inquiry = new ListingInquiry();
        inquiry.setListing(listing);
        inquiry.setStudentId(studentId);
        inquiry.setMessage(request.getMessage());
        inquiry.setPreferredMoveInDate(request.getPreferredMoveInDate());
        inquiry.setLeaseDurationRequested(request.getLeaseDurationRequested());
        inquiry.setStatus(InquiryStatus.PENDING);
        
        ListingInquiry savedInquiry = inquiryRepository.save(inquiry);
        return convertToResponse(savedInquiry);
    }

    // Get inquiry by ID
    public Optional<InquiryResponse> getInquiryById(Long inquiryId) {
        Optional<ListingInquiry> inquiry = inquiryRepository.findById(inquiryId);
        return inquiry.map(this::convertToResponse);
    }

    // Get inquiries for a listing (landlord view)
    public Page<InquiryResponse> getListingInquiries(Long listingId, Long landlordId, int page, int size) {
        // Verify ownership
        Listing listing = listingRepository.findById(listingId)
            .orElseThrow(() -> new IllegalArgumentException("Listing not found with ID: " + listingId));
        
        if (!listing.getLandlordId().equals(landlordId)) {
            throw new IllegalArgumentException("You don't have permission to view inquiries for this listing");
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ListingInquiry> inquiries = inquiryRepository.findByListingId(listingId, pageable);
        
        return inquiries.map(this::convertToResponse);
    }

    // Get all inquiries for landlord
    public Page<InquiryResponse> getLandlordInquiries(Long landlordId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ListingInquiry> inquiries = inquiryRepository.findByLandlordIdOrderByCreatedAtDesc(landlordId, pageable);
        
        return inquiries.map(this::convertToResponse);
    }

    // Get inquiries by student
    public Page<InquiryResponse> getStudentInquiries(Long studentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ListingInquiry> inquiries = inquiryRepository.findByStudentId(studentId, pageable);
        
        return inquiries.map(this::convertToResponse);
    }

    // Get pending inquiries for landlord
    public List<InquiryResponse> getPendingInquiries(Long landlordId) {
        List<ListingInquiry> inquiries = inquiryRepository.findByLandlordIdAndStatus(landlordId, InquiryStatus.PENDING);
        return inquiries.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Respond to inquiry (landlord)
    @Transactional
    public InquiryResponse respondToInquiry(Long inquiryId, Long landlordId, RespondToInquiryRequest request) {
        ListingInquiry inquiry = inquiryRepository.findById(inquiryId)
            .orElseThrow(() -> new IllegalArgumentException("Inquiry not found with ID: " + inquiryId));
        
        // Verify ownership
        if (!inquiry.getListing().getLandlordId().equals(landlordId)) {
            throw new IllegalArgumentException("You don't have permission to respond to this inquiry");
        }
        
        if (inquiry.getStatus() != InquiryStatus.PENDING) {
            throw new IllegalArgumentException("This inquiry has already been responded to");
        }
        
        inquiry.setLandlordResponse(request.getResponse());
        inquiry.setStatus(request.getStatus());
        inquiry.setRespondedAt(LocalDateTime.now());
        
        ListingInquiry savedInquiry = inquiryRepository.save(inquiry);
        return convertToResponse(savedInquiry);
    }

    // Update inquiry status
    @Transactional
    public InquiryResponse updateInquiryStatus(Long inquiryId, Long landlordId, InquiryStatus status) {
        ListingInquiry inquiry = inquiryRepository.findById(inquiryId)
            .orElseThrow(() -> new IllegalArgumentException("Inquiry not found with ID: " + inquiryId));
        
        // Verify ownership
        if (!inquiry.getListing().getLandlordId().equals(landlordId)) {
            throw new IllegalArgumentException("You don't have permission to update this inquiry");
        }
        
        inquiry.setStatus(status);
        ListingInquiry savedInquiry = inquiryRepository.save(inquiry);
        
        return convertToResponse(savedInquiry);
    }

    // Archive inquiry
    @Transactional
    public void archiveInquiry(Long inquiryId, Long userId) {
        ListingInquiry inquiry = inquiryRepository.findById(inquiryId)
            .orElseThrow(() -> new IllegalArgumentException("Inquiry not found with ID: " + inquiryId));
        
        // Verify ownership (either landlord or student)
        boolean isLandlord = inquiry.getListing().getLandlordId().equals(userId);
        boolean isStudent = inquiry.getStudentId().equals(userId);
        
        if (!isLandlord && !isStudent) {
            throw new IllegalArgumentException("You don't have permission to archive this inquiry");
        }
        
        inquiry.setStatus(InquiryStatus.ARCHIVED);
        inquiryRepository.save(inquiry);
    }

    // Delete inquiry (only by student who created it)
    @Transactional
    public void deleteInquiry(Long inquiryId, Long studentId) {
        ListingInquiry inquiry = inquiryRepository.findById(inquiryId)
            .orElseThrow(() -> new IllegalArgumentException("Inquiry not found with ID: " + inquiryId));
        
        // Verify ownership
        if (!inquiry.getStudentId().equals(studentId)) {
            throw new IllegalArgumentException("You don't have permission to delete this inquiry");
        }
        
        // Only allow deletion if not yet responded to
        if (inquiry.getStatus() != InquiryStatus.PENDING) {
            throw new IllegalArgumentException("Cannot delete inquiry that has been responded to");
        }
        
        inquiryRepository.delete(inquiry);
    }

    // Get inquiry statistics for landlord
    public Map<String, Object> getLandlordInquiryStats(Long landlordId) {
        Long totalInquiries = inquiryRepository.countByListingId(landlordId);
        Long pendingInquiries = inquiryRepository.countPendingInquiriesByLandlord(landlordId);
        
        List<ListingInquiry> allInquiries = inquiryRepository.findByLandlordId(landlordId);
        
        long respondedInquiries = allInquiries.stream()
            .filter(inquiry -> inquiry.getStatus() == InquiryStatus.RESPONDED || 
                             inquiry.getStatus() == InquiryStatus.ACCEPTED || 
                             inquiry.getStatus() == InquiryStatus.DECLINED)
            .count();
        
        long acceptedInquiries = allInquiries.stream()
            .filter(inquiry -> inquiry.getStatus() == InquiryStatus.ACCEPTED)
            .count();
        
        double responseRate = totalInquiries > 0 ? (double) respondedInquiries / totalInquiries * 100 : 0.0;
        double acceptanceRate = respondedInquiries > 0 ? (double) acceptedInquiries / respondedInquiries * 100 : 0.0;
        
        return Map.of(
            "totalInquiries", totalInquiries,
            "pendingInquiries", pendingInquiries,
            "respondedInquiries", respondedInquiries,
            "acceptedInquiries", acceptedInquiries,
            "responseRate", Math.round(responseRate * 100.0) / 100.0,
            "acceptanceRate", Math.round(acceptanceRate * 100.0) / 100.0
        );
    }

    // Helper methods
    private void validateInquiryRequest(CreateInquiryRequest request) {
        if (request.getMessage() == null || request.getMessage().trim().length() < 10) {
            throw new IllegalArgumentException("Message must be at least 10 characters long");
        }
        
        if (request.getMessage().length() > 1000) {
            throw new IllegalArgumentException("Message cannot exceed 1000 characters");
        }
        
        if (request.getLeaseDurationRequested() != null && 
            (request.getLeaseDurationRequested() < 1 || request.getLeaseDurationRequested() > 24)) {
            throw new IllegalArgumentException("Lease duration must be between 1 and 24 months");
        }
    }

    private InquiryResponse convertToResponse(ListingInquiry inquiry) {
        InquiryResponse response = new InquiryResponse();
        
        response.setId(inquiry.getId());
        response.setListingId(inquiry.getListing().getId());
        response.setListingTitle(inquiry.getListing().getTitle());
        response.setStudentId(inquiry.getStudentId());
        response.setLandlordId(inquiry.getListing().getLandlordId());
        response.setMessage(inquiry.getMessage());
        response.setPreferredMoveInDate(inquiry.getPreferredMoveInDate());
        response.setLeaseDurationRequested(inquiry.getLeaseDurationRequested());
        response.setStatus(inquiry.getStatus());
        response.setLandlordResponse(inquiry.getLandlordResponse());
        response.setRespondedAt(inquiry.getRespondedAt());
        response.setCreatedAt(inquiry.getCreatedAt());
        response.setUpdatedAt(inquiry.getUpdatedAt());
        
        return response;
    }
}