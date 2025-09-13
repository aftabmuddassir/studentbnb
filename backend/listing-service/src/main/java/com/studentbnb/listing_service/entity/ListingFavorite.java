package com.studentbnb.listing_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "listing_favorites", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"listing_id", "user_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListingFavorite {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;
    
    @Column(name = "user_id", nullable = false)
    private Long userId; // Reference to User ID from user-service
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}