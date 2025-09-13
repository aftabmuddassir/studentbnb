package com.studentbnb.listing_service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class ListingStatsResponse {
    
    private Long listingId;
    private String listingTitle;
    
    // View statistics
    private Integer totalViews;
    private Integer uniqueViews;
    private Integer viewsThisWeek;
    private Integer viewsThisMonth;
    
    // Favorite statistics
    private Integer totalFavorites;
    private Integer favoritesThisWeek;
    private Integer favoritesThisMonth;
    
    // Inquiry statistics
    private Integer totalInquiries;
    private Integer pendingInquiries;
    private Integer respondedInquiries;
    private Integer inquiriesThisWeek;
    private Integer inquiriesThisMonth;
    
    // Performance metrics
    private Double inquiryRate; // inquiries per view
    private Double favoriteRate; // favorites per view
    private Double responseRate; // responded inquiries / total inquiries
    
    // Weekly trends (last 4 weeks)
    private Map<String, Integer> weeklyViews;
    private Map<String, Integer> weeklyInquiries;
    private Map<String, Integer> weeklyFavorites;
}