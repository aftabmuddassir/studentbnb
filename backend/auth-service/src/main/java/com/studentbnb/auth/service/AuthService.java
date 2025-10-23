package com.studentbnb.auth.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.studentbnb.auth.dto.LoginRequest;
import com.studentbnb.auth.dto.RegisterRequest;
import com.studentbnb.auth.entity.User;
import com.studentbnb.auth.entity.UserRole;
import com.studentbnb.auth.repository.UserRepository;

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

    public User updateUserProfile(Long userId, com.studentbnb.auth.dto.UpdateProfileRequest request) throws IllegalArgumentException {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Update only provided fields
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName().trim());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber().trim());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio().trim());
        }
        if (request.getUniversity() != null) {
            user.setUniversity(request.getUniversity().trim());
        }
        if (request.getGraduationYear() != null) {
            user.setGraduationYear(request.getGraduationYear());
        }
        if (request.getCity() != null) {
            user.setCity(request.getCity().trim());
        }
        if (request.getState() != null) {
            user.setState(request.getState().trim());
        }
        if (request.getCountry() != null) {
            user.setCountry(request.getCountry().trim());
        }
        if (request.getZipcode() != null) {
            user.setZipcode(request.getZipcode().trim());
        }

        return userRepository.save(user);
    }
}