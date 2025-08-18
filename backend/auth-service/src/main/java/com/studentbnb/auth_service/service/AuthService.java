package com.studentbnb.auth_service.service;


import com.studentbnb.auth_service.dto.LoginRequest;
import com.studentbnb.auth_service.dto.RegisterRequest;
import com.studentbnb.auth_service.entity.User;
import com.studentbnb.auth_service.entity.UserRole;
import com.studentbnb.auth_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(RegisterRequest request) throws IllegalArgumentException {
        // Validation
        validateRegistrationRequest(request);
        
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("User with this email already exists");
        }

        // Create new user
        User user = new User();
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : UserRole.STUDENT);

        return userRepository.save(user);
    }

    public User authenticateUser(LoginRequest request) throws IllegalArgumentException {
        // Validation
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        // Find user by email
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail().trim().toLowerCase());
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        User user = userOpt.get();

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return user;
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase());
    }

    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    private void validateRegistrationRequest(RegisterRequest request) throws IllegalArgumentException {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        
        if (!isValidEmail(request.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }
        
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }
        
        // Add more password validation if needed
        if (!isValidPassword(request.getPassword())) {
            throw new IllegalArgumentException("Password must contain at least one letter and one number");
        }
    }

    private boolean isValidEmail(String email) {
        // Simple email validation 
        return email.contains("@") && email.contains(".");
    }

    private boolean isValidPassword(String password) {
        // Password must contain at least one letter and one number
        return password.matches(".*[a-zA-Z].*") && password.matches(".*[0-9].*");
    }
}