package com.studentbnb.user_service.service;

import com.studentbnb.user_service.entity.RoommatePreferences;
import com.studentbnb.user_service.entity.User;
import com.studentbnb.user_service.repository.RoommatePreferencesRepository;
import com.studentbnb.user_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;

@Service
public class RoommatePreferencesService {

    @Autowired
    private RoommatePreferencesRepository preferencesRepository;
    
    @Autowired
    private UserRepository userRepository;

    // Create or update roommate preferences
    @Transactional
    public RoommatePreferences savePreferences(Long userId, RoommatePreferences preferences) {
        // Verify user exists
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        // Check if preferences already exist
        Optional<RoommatePreferences> existingPrefs = preferencesRepository.findByUserId(userId);
        
        if (existingPrefs.isPresent()) {
            // Update existing preferences
            RoommatePreferences existing = existingPrefs.get();
            updatePreferencesFields(existing, preferences);
            return preferencesRepository.save(existing);
        } else {
            // Create new preferences
            preferences.setUser(user);
            return preferencesRepository.save(preferences);
        }
    }

    // Get preferences by user ID
    public Optional<RoommatePreferences> getPreferences(Long userId) {
        return preferencesRepository.findByUserId(userId);
    }

    // Find compatible roommates
    public List<RoommatePreferences> findCompatibleRoommates(Long userId) {
        // Get current user's preferences
        RoommatePreferences userPrefs = preferencesRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("User preferences not found"));
        
        // Find compatible roommates based on key criteria
        return preferencesRepository.findCompatibleRoommates(
            userId,
            userPrefs.getBudgetMin(),
            userPrefs.getBudgetMax(),
            userPrefs.getSmokingPreference(),
            userPrefs.getPetsAllowed()
        );
    }

    // Find roommates by budget range
    public List<RoommatePreferences> findByBudgetRange(Integer minBudget, Integer maxBudget) {
        return preferencesRepository.findByBudgetRange(minBudget, maxBudget);
    }

    // Find roommates by smoking preference
    public List<RoommatePreferences> findBySmokingPreference(Boolean smoking) {
        return preferencesRepository.findBySmokingPreference(smoking);
    }

    // Find roommates who allow pets
    public List<RoommatePreferences> findByPetsAllowed(Boolean petsAllowed) {
        return preferencesRepository.findByPetsAllowed(petsAllowed);
    }

    // Calculate compatibility score between two users
    public double calculateCompatibilityScore(Long userId1, Long userId2) {
        RoommatePreferences prefs1 = preferencesRepository.findByUserId(userId1)
            .orElseThrow(() -> new IllegalArgumentException("Preferences not found for user: " + userId1));
        
        RoommatePreferences prefs2 = preferencesRepository.findByUserId(userId2)
            .orElseThrow(() -> new IllegalArgumentException("Preferences not found for user: " + userId2));
        
        return calculateScore(prefs1, prefs2);
    }

    // Delete preferences
    @Transactional
    public void deletePreferences(Long userId) {
        preferencesRepository.findByUserId(userId)
            .ifPresent(prefs -> preferencesRepository.delete(prefs));
    }

    // Helper method to update preferences fields
    private void updatePreferencesFields(RoommatePreferences existing, RoommatePreferences updated) {
        existing.setCleanlinessLevel(updated.getCleanlinessLevel());
        existing.setNoiseTolerance(updated.getNoiseTolerance());
        existing.setQuietHoursStart(updated.getQuietHoursStart());
        existing.setQuietHoursEnd(updated.getQuietHoursEnd());
        existing.setSmokingPreference(updated.getSmokingPreference());
        existing.setDrinkingPreference(updated.getDrinkingPreference());
        existing.setPetsAllowed(updated.getPetsAllowed());
        existing.setHasPets(updated.getHasPets());
        existing.setDietType(updated.getDietType());
        existing.setCookingFrequency(updated.getCookingFrequency());
        existing.setKitchenSharing(updated.getKitchenSharing());
        existing.setSocialLevel(updated.getSocialLevel());
        existing.setGuestsFrequency(updated.getGuestsFrequency());
        existing.setStudyHoursStart(updated.getStudyHoursStart());
        existing.setStudyHoursEnd(updated.getStudyHoursEnd());
        existing.setStudyLocationPreference(updated.getStudyLocationPreference());
        existing.setBudgetMin(updated.getBudgetMin());
        existing.setBudgetMax(updated.getBudgetMax());
        existing.setUtilitiesIncluded(updated.getUtilitiesIncluded());
        existing.setGenderPreference(updated.getGenderPreference());
    }

    // Helper method to calculate compatibility score
    private double calculateScore(RoommatePreferences prefs1, RoommatePreferences prefs2) {
        double score = 0.0;
        int totalCriteria = 0;

        // Budget compatibility (weight: 25%)
        if (prefs1.getBudgetMin() != null && prefs1.getBudgetMax() != null &&
            prefs2.getBudgetMin() != null && prefs2.getBudgetMax() != null) {
            
            boolean budgetOverlap = !(prefs1.getBudgetMax() < prefs2.getBudgetMin() || 
                                    prefs2.getBudgetMax() < prefs1.getBudgetMin());
            if (budgetOverlap) score += 25;
            totalCriteria += 25;
        }

        // Smoking compatibility (weight: 20%)
        if (prefs1.getSmokingPreference() != null && prefs2.getSmokingPreference() != null) {
            if (prefs1.getSmokingPreference().equals(prefs2.getSmokingPreference())) {
                score += 20;
            }
            totalCriteria += 20;
        }

        // Pet compatibility (weight: 15%)
        if (prefs1.getPetsAllowed() != null && prefs2.getHasPets() != null) {
            if (prefs1.getPetsAllowed() || !prefs2.getHasPets()) {
                score += 15;
            }
            totalCriteria += 15;
        }

        // Cleanliness compatibility (weight: 15%)
        if (prefs1.getCleanlinessLevel() != null && prefs2.getCleanlinessLevel() != null) {
            if (prefs1.getCleanlinessLevel().equals(prefs2.getCleanlinessLevel())) {
                score += 15;
            }
            totalCriteria += 15;
        }

        // Social level compatibility (weight: 10%)
        if (prefs1.getSocialLevel() != null && prefs2.getSocialLevel() != null) {
            if (prefs1.getSocialLevel().equals(prefs2.getSocialLevel())) {
                score += 10;
            }
            totalCriteria += 10;
        }

        // Noise tolerance compatibility (weight: 10%)
        if (prefs1.getNoiseTolerance() != null && prefs2.getNoiseTolerance() != null) {
            if (prefs1.getNoiseTolerance().equals(prefs2.getNoiseTolerance())) {
                score += 10;
            }
            totalCriteria += 10;
        }

        // Kitchen sharing compatibility (weight: 5%)
        if (prefs1.getKitchenSharing() != null && prefs2.getKitchenSharing() != null) {
            if (prefs1.getKitchenSharing().equals(prefs2.getKitchenSharing())) {
                score += 5;
            }
            totalCriteria += 5;
        }

        return totalCriteria > 0 ? (score / totalCriteria) * 100 : 0.0;
    }
}