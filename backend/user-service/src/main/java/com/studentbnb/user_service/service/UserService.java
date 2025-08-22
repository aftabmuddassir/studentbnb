package com.studentbnb.user_service.service;

import com.studentbnb.user_service.dto.CreateUserProfileRequest;
import com.studentbnb.user_service.dto.UserProfileResponse;
import com.studentbnb.user_service.entity.User;
import com.studentbnb.user_service.entity.UserProfile;
import com.studentbnb.user_service.entity.UserRole;
import com.studentbnb.user_service.repository.UserRepository;
import com.studentbnb.user_service.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserProfileRepository userProfileRepository;

    // Create a user (called when auth service registers someone)
    @Transactional
    public User createUser(Long userId, String email, UserRole role) {
        // Check if user already exists
        if (userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User with ID " + userId + " already exists");
        }
        
        User user = new User(userId, email, role);
        return userRepository.save(user);
    }

    // Get user by ID
    public Optional<User> findUserById(Long userId) {
        return userRepository.findById(userId);
    }

    // Get user by email
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Create user profile
    @Transactional
    public UserProfileResponse createUserProfile(Long userId, CreateUserProfileRequest request) {
        // Find the user
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        // Check if profile already exists
        if (userProfileRepository.findByUserId(userId).isPresent()) {
            throw new IllegalArgumentException("Profile already exists for user: " + userId);
        }
        
        // Create new profile
        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setBio(request.getBio());
        profile.setProfileImageUrl(request.getProfileImageUrl());
        profile.setUniversityName(request.getUniversityName());
        profile.setMajor(request.getMajor());
        profile.setGraduationYear(request.getGraduationYear());
        profile.setAddress(request.getAddress());
        profile.setCity(request.getCity());
        profile.setState(request.getState());
        profile.setZipCode(request.getZipCode());
        
        UserProfile savedProfile = userProfileRepository.save(profile);
        
        return convertToProfileResponse(user, savedProfile);
    }

    // Get user profile
    public Optional<UserProfileResponse> getUserProfile(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
        
        User user = userOpt.get();
        Optional<UserProfile> profileOpt = userProfileRepository.findByUserId(userId);
        
        if (profileOpt.isPresent()) {
            return Optional.of(convertToProfileResponse(user, profileOpt.get()));
        } else {
            // Return basic user info even if no profile exists
            return Optional.of(convertToBasicProfileResponse(user));
        }
    }

    // Update user profile
    @Transactional
    public UserProfileResponse updateUserProfile(Long userId, CreateUserProfileRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        UserProfile profile = userProfileRepository.findByUserId(userId)
            .orElse(new UserProfile());
        
        // If it's a new profile, set the user
        if (profile.getUser() == null) {
            profile.setUser(user);
        }
        
        // Update fields
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setBio(request.getBio());
        profile.setProfileImageUrl(request.getProfileImageUrl());
        profile.setUniversityName(request.getUniversityName());
        profile.setMajor(request.getMajor());
        profile.setGraduationYear(request.getGraduationYear());
        profile.setAddress(request.getAddress());
        profile.setCity(request.getCity());
        profile.setState(request.getState());
        profile.setZipCode(request.getZipCode());
        
        UserProfile savedProfile = userProfileRepository.save(profile);
        return convertToProfileResponse(user, savedProfile);
    }

    // Get all users by role
    public List<UserProfileResponse> getUsersByRole(UserRole role) {
        List<User> users = userRepository.findActiveVerifiedUsersByRole(role);
        return users.stream()
            .map(user -> {
                Optional<UserProfile> profile = userProfileRepository.findByUserId(user.getId());
                return profile.map(p -> convertToProfileResponse(user, p))
                             .orElse(convertToBasicProfileResponse(user));
            })
            .collect(Collectors.toList());
    }

    // Get users by university
    public List<UserProfileResponse> getUsersByUniversity(String universityName) {
        List<UserProfile> profiles = userProfileRepository.findByUniversityName(universityName);
        return profiles.stream()
            .map(profile -> convertToProfileResponse(profile.getUser(), profile))
            .collect(Collectors.toList());
    }

    // Get users by city
    public List<UserProfileResponse> getUsersByCity(String city) {
        List<UserProfile> profiles = userProfileRepository.findByCity(city);
        return profiles.stream()
            .map(profile -> convertToProfileResponse(profile.getUser(), profile))
            .collect(Collectors.toList());
    }

    // Verify user (admin function)
    @Transactional
    public void verifyUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        user.setIsVerified(true);
        userRepository.save(user);
    }

    // Deactivate user (admin function)
    @Transactional
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        user.setIsActive(false);
        userRepository.save(user);
    }

    // Helper method to convert to response DTO
    private UserProfileResponse convertToProfileResponse(User user, UserProfile profile) {
        UserProfileResponse response = new UserProfileResponse();
        
        // User information
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().toString());
        response.setIsVerified(user.getIsVerified());
        response.setIsActive(user.getIsActive());
        
        // Profile information
        response.setFirstName(profile.getFirstName());
        response.setLastName(profile.getLastName());
       // response.setFullName(profile.getFullName());
        response.setPhoneNumber(profile.getPhoneNumber());
        response.setDateOfBirth(profile.getDateOfBirth());
        response.setBio(profile.getBio());
        response.setProfileImageUrl(profile.getProfileImageUrl());
        response.setUniversityName(profile.getUniversityName());
        response.setMajor(profile.getMajor());
        response.setGraduationYear(profile.getGraduationYear());
        response.setAddress(profile.getAddress());
        response.setCity(profile.getCity());
        response.setState(profile.getState());
        response.setZipCode(profile.getZipCode());
        response.setCreatedAt(profile.getCreatedAt());
        response.setUpdatedAt(profile.getUpdatedAt());
        
        return response;
    }

    // Helper method for basic user response (when no profile exists)
    private UserProfileResponse convertToBasicProfileResponse(User user) {
        UserProfileResponse response = new UserProfileResponse();
        
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().toString());
        response.setIsVerified(user.getIsVerified());
        response.setIsActive(user.getIsActive());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        
        return response;
    }
}