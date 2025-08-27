package com.studentbnb.user_service.service;

import com.studentbnb.user_service.entity.*;
import com.studentbnb.user_service.repository.RoommatePreferencesRepository;
import com.studentbnb.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoommatePreferencesServiceTest {

    @Mock
    private RoommatePreferencesRepository preferencesRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RoommatePreferencesService preferencesService;

    private User testUser;
    private RoommatePreferences testPreferences;

    @BeforeEach
    void setUp() {
        testUser = new User(1L, "test@university.edu", UserRole.STUDENT);
        
        testPreferences = new RoommatePreferences();
        testPreferences.setUser(testUser);
        testPreferences.setCleanlinessLevel(CleanlinessLevel.CLEAN);
        testPreferences.setNoiseTolerance(NoiseLevel.QUIET);
        testPreferences.setSmokingPreference(false);
        testPreferences.setPetsAllowed(true);
        testPreferences.setBudgetMin(800);
        testPreferences.setBudgetMax(1200);
        testPreferences.setSocialLevel(SocialLevel.MODERATE);
    }

    @Test
    void savePreferences_NewPreferences_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(preferencesRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(preferencesRepository.save(any(RoommatePreferences.class))).thenReturn(testPreferences);

        // Act
        RoommatePreferences result = preferencesService.savePreferences(1L, testPreferences);

        // Assert
        assertNotNull(result);
        assertEquals(CleanlinessLevel.CLEAN, result.getCleanlinessLevel());
        verify(preferencesRepository).save(any(RoommatePreferences.class));
    }

    @Test
    void savePreferences_UpdateExisting_Success() {
        // Arrange
        RoommatePreferences existingPrefs = new RoommatePreferences();
        existingPrefs.setUser(testUser);
        existingPrefs.setCleanlinessLevel(CleanlinessLevel.MODERATE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(preferencesRepository.findByUserId(1L)).thenReturn(Optional.of(existingPrefs));
        when(preferencesRepository.save(any(RoommatePreferences.class))).thenReturn(existingPrefs);

        // Act
        RoommatePreferences result = preferencesService.savePreferences(1L, testPreferences);

        // Assert
        assertNotNull(result);
        verify(preferencesRepository).save(any(RoommatePreferences.class));
    }

    @Test
    void savePreferences_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> preferencesService.savePreferences(1L, testPreferences)
        );

        assertEquals("User not found with ID: 1", exception.getMessage());
        verify(preferencesRepository, never()).save(any(RoommatePreferences.class));
    }

    @Test
    void getPreferences_Success() {
        // Arrange
        when(preferencesRepository.findByUserId(1L)).thenReturn(Optional.of(testPreferences));

        // Act
        Optional<RoommatePreferences> result = preferencesService.getPreferences(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(CleanlinessLevel.CLEAN, result.get().getCleanlinessLevel());
    }

    @Test
    void findCompatibleRoommates_Success() {
        // Arrange
        when(preferencesRepository.findByUserId(1L)).thenReturn(Optional.of(testPreferences));
        
        List<RoommatePreferences> compatibleList = Arrays.asList(testPreferences);
        when(preferencesRepository.findCompatibleRoommates(anyLong(), anyInt(), anyInt(), anyBoolean(), anyBoolean()))
            .thenReturn(compatibleList);

        // Act
        List<RoommatePreferences> result = preferencesService.findCompatibleRoommates(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(preferencesRepository).findCompatibleRoommates(1L, 800, 1200, false, true);
    }

    @Test
    void findCompatibleRoommates_UserPreferencesNotFound_ThrowsException() {
        // Arrange
        when(preferencesRepository.findByUserId(1L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> preferencesService.findCompatibleRoommates(1L)
        );

        assertEquals("User preferences not found", exception.getMessage());
    }

    @Test
    void findByBudgetRange_Success() {
        // Arrange
        List<RoommatePreferences> expectedList = Arrays.asList(testPreferences);
        when(preferencesRepository.findByBudgetRange(800, 1200)).thenReturn(expectedList);

        // Act
        List<RoommatePreferences> result = preferencesService.findByBudgetRange(800, 1200);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(preferencesRepository).findByBudgetRange(800, 1200);
    }

    @Test
    void findBySmokingPreference_Success() {
        // Arrange
        List<RoommatePreferences> expectedList = Arrays.asList(testPreferences);
        when(preferencesRepository.findBySmokingPreference(false)).thenReturn(expectedList);

        // Act
        List<RoommatePreferences> result = preferencesService.findBySmokingPreference(false);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(preferencesRepository).findBySmokingPreference(false);
    }

    @Test
    void findByPetsAllowed_Success() {
        // Arrange
        List<RoommatePreferences> expectedList = Arrays.asList(testPreferences);
        when(preferencesRepository.findByPetsAllowed(true)).thenReturn(expectedList);

        // Act
        List<RoommatePreferences> result = preferencesService.findByPetsAllowed(true);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(preferencesRepository).findByPetsAllowed(true);
    }

    @Test
    void calculateCompatibilityScore_Success() {
        // Arrange - Create two users with preferences
        User user2 = new User(2L, "user2@university.edu", UserRole.STUDENT);
        RoommatePreferences prefs2 = new RoommatePreferences();
        prefs2.setUser(user2);
        prefs2.setCleanlinessLevel(CleanlinessLevel.CLEAN);
        prefs2.setSmokingPreference(false);
        prefs2.setPetsAllowed(true);
        prefs2.setBudgetMin(900);
        prefs2.setBudgetMax(1100);
        prefs2.setSocialLevel(SocialLevel.MODERATE);
        prefs2.setNoiseTolerance(NoiseLevel.QUIET);
        prefs2.setKitchenSharing(true);

        when(preferencesRepository.findByUserId(1L)).thenReturn(Optional.of(testPreferences));
        when(preferencesRepository.findByUserId(2L)).thenReturn(Optional.of(prefs2));

        // Act
        double score = preferencesService.calculateCompatibilityScore(1L, 2L);

        // Assert
        assertTrue(score > 0);
        assertTrue(score <= 100);
    }

    @Test
    void calculateCompatibilityScore_FirstUserNotFound_ThrowsException() {
        // Arrange
        when(preferencesRepository.findByUserId(1L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> preferencesService.calculateCompatibilityScore(1L, 2L)
        );

        assertEquals("Preferences not found for user: 1", exception.getMessage());
    }

    @Test
    void calculateCompatibilityScore_SecondUserNotFound_ThrowsException() {
        // Arrange
        when(preferencesRepository.findByUserId(1L)).thenReturn(Optional.of(testPreferences));
        when(preferencesRepository.findByUserId(2L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> preferencesService.calculateCompatibilityScore(1L, 2L)
        );

        assertEquals("Preferences not found for user: 2", exception.getMessage());
    }

    @Test
    void deletePreferences_Success() {
        // Arrange
        when(preferencesRepository.findByUserId(1L)).thenReturn(Optional.of(testPreferences));

        // Act
        preferencesService.deletePreferences(1L);

        // Assert
        verify(preferencesRepository).delete(testPreferences);
    }

    @Test
    void deletePreferences_NotFound_NoException() {
        // Arrange
        when(preferencesRepository.findByUserId(1L)).thenReturn(Optional.empty());

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> preferencesService.deletePreferences(1L));
        verify(preferencesRepository, never()).delete(any());
    }

    @Test
    void calculateScore_PerfectMatch_ReturnsHighScore() {
        // Arrange - Create identical preferences
        User user2 = new User(2L, "user2@university.edu", UserRole.STUDENT);
        RoommatePreferences identicalPrefs = new RoommatePreferences();
        identicalPrefs.setUser(user2);
        identicalPrefs.setCleanlinessLevel(CleanlinessLevel.CLEAN);
        identicalPrefs.setSmokingPreference(false);
        identicalPrefs.setPetsAllowed(true);
        identicalPrefs.setHasPets(false);
        identicalPrefs.setBudgetMin(800);
        identicalPrefs.setBudgetMax(1200);
        identicalPrefs.setSocialLevel(SocialLevel.MODERATE);
        identicalPrefs.setNoiseTolerance(NoiseLevel.QUIET);
        identicalPrefs.setKitchenSharing(true);

        testPreferences.setHasPets(false);
        testPreferences.setKitchenSharing(true);

        when(preferencesRepository.findByUserId(1L)).thenReturn(Optional.of(testPreferences));
        when(preferencesRepository.findByUserId(2L)).thenReturn(Optional.of(identicalPrefs));

        // Act
        double score = preferencesService.calculateCompatibilityScore(1L, 2L);

        // Assert
        assertEquals(100.0, score, 0.1); // Perfect match should give 100%
    }

    @Test
    void calculateScore_NoOverlap_ReturnsLowScore() {
        // Arrange - Create completely incompatible preferences
        User user2 = new User(2L, "user2@university.edu", UserRole.STUDENT);
        RoommatePreferences incompatiblePrefs = new RoommatePreferences();
        incompatiblePrefs.setUser(user2);
        incompatiblePrefs.setSmokingPreference(true); // Different from testPreferences (false)
        incompatiblePrefs.setPetsAllowed(false); // Different from testPreferences (true) 
        incompatiblePrefs.setHasPets(true);
        incompatiblePrefs.setBudgetMin(2000); // No overlap with testPreferences (800-1200)
        incompatiblePrefs.setBudgetMax(3000);

        testPreferences.setHasPets(false);

        when(preferencesRepository.findByUserId(1L)).thenReturn(Optional.of(testPreferences));
        when(preferencesRepository.findByUserId(2L)).thenReturn(Optional.of(incompatiblePrefs));

        // Act
        double score = preferencesService.calculateCompatibilityScore(1L, 2L);

        // Assert
        assertTrue(score < 50.0); // Should be low compatibility
    }
}