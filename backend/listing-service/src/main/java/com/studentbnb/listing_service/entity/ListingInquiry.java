package com.studentbnb.listing_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "listing_inquiries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListingInquiry {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;
    
    @Column(name = "student_id", nullable = false)
    private Long studentId; // Reference to User ID from user-service
    
    @NotBlank
    @Size(min = 10, max = 1000)
    @Column(nullable = false, length = 1000)
    private String message;
    
    @Column(name = "preferred_move_in_date")
    private LocalDate preferredMoveInDate;
    
    @Column(name = "lease_duration_requested")
    private Integer leaseDurationRequested; // in months
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InquiryStatus status = InquiryStatus.PENDING;
    
    @Column(name = "landlord_response", length = 1000)
    private String landlordResponse;
    
    @Column(name = "responded_at")
    private LocalDateTime respondedAt;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}