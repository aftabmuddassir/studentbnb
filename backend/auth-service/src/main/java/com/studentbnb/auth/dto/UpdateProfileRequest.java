package com.studentbnb.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String bio;
    private String university;
    private Integer graduationYear;
    private String city;
    private String state;
    private String country;
    private String zipcode;
}
