package com.studentbnb.auth_service.dto;

import com.studentbnb.auth_service.entity.UserRole;
import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    private UserRole role;
}