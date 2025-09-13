package com.studentbnb.listing_service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class UpdateListingRequest {
    
    @Size(min = 10, max = 100, message = "Title must be between 10 and 100 characters")
    private String title;
    
    @Size(min = 50, max = 2000, message = "Description must be between 50 and 2000 characters")
    private String description;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Rent must be greater than 0")
    private BigDecimal rent;
    
    @DecimalMin(value = "0.0", message = "Security deposit must be 0 or greater")
    private BigDecimal securityDeposit;
    
    private Boolean utilitiesIncluded;
    
    // Availability
    private LocalDate availableFrom;
    private LocalDate availableUntil;
    
    // Preferences
    private Boolean petsAllowed;
    private Boolean smokingAllowed;
    private Boolean furnished;
    
    // Contact
    @Email(message = "Invalid email format")
    private String contactEmail;
    
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Invalid phone number format")
    private String contactPhone;
}