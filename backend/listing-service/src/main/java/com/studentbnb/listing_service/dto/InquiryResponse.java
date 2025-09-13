package com.studentbnb.listing_service.dto;

import com.studentbnb.listing_service.entity.InquiryStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class InquiryResponse {
    
    private Long id;
    private Long listingId;
    private String listingTitle;
    private Long studentId;
    private Long landlordId;
    
    // Inquiry details
    private String message;
    private LocalDate preferredMoveInDate;
    private Integer leaseDurationRequested;
    
    // Response details
    private InquiryStatus status;
    private String landlordResponse;
    private LocalDateTime respondedAt;
    
    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}