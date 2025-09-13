package com.studentbnb.listing_service.dto;


import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class CreateInquiryRequest {
    
    @NotBlank(message = "Message is required")
    @Size(min = 10, max = 1000, message = "Message must be between 10 and 1000 characters")
    private String message;
    
    @Future(message = "Move-in date must be in the future")
    private LocalDate preferredMoveInDate;
    
    @Min(value = 1, message = "Lease duration must be at least 1 month")
    @Max(value = 24, message = "Lease duration cannot exceed 24 months")
    private Integer leaseDurationRequested;
}