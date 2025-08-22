package com.studentbnb.auth.dto;

import com.studentbnb.auth.entity.UserRole;

import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    private UserRole role;
}