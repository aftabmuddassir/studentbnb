package com.studentbnb.user_service.service;

import com.studentbnb.user_service.dto.CreateUserProfileRequest;
import com.studentbnb.user_service.dto.UserProfileResponse;
import com.studentbnb.user_service.entity.User;
import com.studentbnb.user_service.entity.UserProfile;
import com.studentbnb.user_service.entity.UserRole;
import com.studentbnb.user_service.repository.UserRepository;
import com.studentbnb.user_service.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserProfile testProfile;
    private CreateUserProfileRequest createRequest;

    @BeforeEach
    void setUp() {
        // Setup test data
        testUser = new User(1L, "test@university.edu", UserRole.STUDENT);
        
        testProfile = new UserProfile();
        testProfile.setUser(testUser);
        testProfile.setFirstName("John");
        testProfile.setLastName("Doe");
        testProfile.setUniversityName("Test University");
        testProfile.setMajor("Computer Science");

        createRequest = new CreateUserProfileRequest();
        createRequest.setFirstName("John");
        createRequest.setLastName("Doe");
        createRequest.setPhoneNumber("+1234567890");
        createRequest.setDateOfBirth(LocalDate.of(2000, 1, 15));
        createRequest.setBio("Test bio");
        createRequest.setUniversityName("Test University");
        createRequest.setMajor("Computer Science");
        createRequest.setGraduationYear(2025);
        createRequest.setCity("Test City");
        createRequest.setState("CA");
        createRequest.setZipCode("90210");
    }

    @Test
    void createUser_Success() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.createUser(1L, "test@university.edu", UserRole.STUDENT);

        // Assert
        assertNotNull(result);
        assertEquals("test@university.edu", result.getEmail());
        assertEquals(UserRole.STUDENT, result.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_UserAlreadyExists_ThrowsException() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.createUser(1L, "test@university.edu", UserRole.STUDENT)
        );

        assertEquals("User with ID 1 already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUserProfile_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(testProfile);

        // Act
        UserProfileResponse result = userService.createUserProfile(1L, createRequest);

        // Assert
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("test@university.edu", result.getEmail());
        verify(userProfileRepository).save(any(UserProfile.class));
    }

    @Test
    void createUserProfile_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.createUserProfile(1L, createRequest)
        );

        assertEquals("User not found with ID: 1", exception.getMessage());
        verify(userProfileRepository, never()).save(any(UserProfile.class));
    }

    @Test
    void createUserProfile_ProfileAlreadyExists_ThrowsException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.createUserProfile(1L, createRequest)
        );

        assertEquals("Profile already exists for user: 1", exception.getMessage());
        verify(userProfileRepository, never()).save(any(UserProfile.class));
    }

    @Test
    void getUserProfile_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));

        // Act
        Optional<UserProfileResponse> result = userService.getUserProfile(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("John", result.get().getFirstName());
        assertEquals("test@university.edu", result.get().getEmail());
    }

    @Test
    void getUserProfile_UserNotFound_ReturnsEmpty() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Optional<UserProfileResponse> result = userService.getUserProfile(1L);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void updateUserProfile_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(testProfile);

        // Modify request data
        createRequest.setFirstName("Jane");
        createRequest.setLastName("Smith");

        // Act
        UserProfileResponse result = userService.updateUserProfile(1L, createRequest);

        // Assert
        assertNotNull(result);
        verify(userProfileRepository).save(any(UserProfile.class));
    }

    @Test
    void verifyUser_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.verifyUser(1L);

        // Assert
        verify(userRepository).save(any(User.class));
    }

    @Test
    void verifyUser_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.verifyUser(1L)
        );

        assertEquals("User not found with ID: 1", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
}