package com.studentbnb.listing_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AddPhotoRequest {
    
    @NotBlank(message = "Photo URL is required")
    private String photoUrl;
    
    @Size(max = 200, message = "Description cannot exceed 200 characters")
    private String description;
    
    private Integer displayOrder;
    
    private Boolean isPrimary = false;
}