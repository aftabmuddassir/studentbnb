package com.studentbnb.listing_service.dto;


import com.studentbnb.listing_service.entity.InquiryStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RespondToInquiryRequest {
    
    @NotBlank(message = "Response is required")
    @Size(min = 10, max = 1000, message = "Response must be between 10 and 1000 characters")
    private String response;
    
    @NotNull(message = "Status is required")
    private InquiryStatus status; // RESPONDED, ACCEPTED, DECLINED
}