package com.studentbnb.auth.dto;

import com.studentbnb.auth.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String email;
    private UserRole role;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String profilePictureUrl;
    private String bio;
    private String university;
    private Integer graduationYear;
    private String city;
    private String state;
    private String country;
    private String zipcode;
    private Boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
