package com.studentbnb.user_service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class CreateUserProfileRequest {
    
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;
    
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Invalid phone number format")
    private String phoneNumber;
    
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;
    
    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;
    
    private String profileImageUrl;
    
    // University information
    @NotBlank(message = "University name is required")
    private String universityName;
    
    private String major;
    
    @Min(value = 2020, message = "Graduation year must be at least 2020")
    @Max(value = 2035, message = "Graduation year must not exceed 2035")
    private Integer graduationYear;
    
    // Address information
    private String address;
    private String city;
    private String state;
    private String zipCode;
    

}