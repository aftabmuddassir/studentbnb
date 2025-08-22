package com.studentbnb.user_service.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserProfileResponse {
    
    private Long userId;
    private String email;
    private String role;
    private Boolean isVerified;
    private Boolean isActive;
    
    // Profile information
    private String firstName;
    private String lastName;
    private String fullName;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String bio;
    private String profileImageUrl;
    
    // University information
    private String universityName;
    private String major;
    private Integer graduationYear;
    
    // Address information
    private String address;
    private String city;
    private String state;
    private String zipCode;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}