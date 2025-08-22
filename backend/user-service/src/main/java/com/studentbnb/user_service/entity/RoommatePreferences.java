package com.studentbnb.user_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "roommate_preferences")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoommatePreferences {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
    
    // Lifestyle preferences
    @Enumerated(EnumType.STRING)
    @Column(name = "cleanliness_level")
    private CleanlinessLevel cleanlinessLevel;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "noise_tolerance")
    private NoiseLevel noiseTolerance;
    
    // Quiet hours
    @Column(name = "quiet_hours_start")
    private LocalTime quietHoursStart;
    
    @Column(name = "quiet_hours_end")  
    private LocalTime quietHoursEnd;
    
    // Personal habits
    @Column(name = "smoking_preference")
    private Boolean smokingPreference; // true = smoker, false = non-smoker
    
    @Column(name = "drinking_preference")
    private Boolean drinkingPreference; // true = drinks, false = doesn't drink
    
    @Column(name = "pets_allowed")
    private Boolean petsAllowed;
    
    @Column(name = "has_pets")
    private Boolean hasPets;
    
    // Diet and cooking
    @Enumerated(EnumType.STRING)
    @Column(name = "diet_type")
    private DietType dietType;
    
    @Column(name = "cooking_frequency")
    private String cookingFrequency; // "rarely", "sometimes", "often", "daily"
    
    @Column(name = "kitchen_sharing")
    private Boolean kitchenSharing;
    
    // Social preferences
    @Enumerated(EnumType.STRING)
    @Column(name = "social_level")
    private SocialLevel socialLevel;
    
    @Column(name = "guests_frequency")
    private String guestsFrequency; // "never", "rarely", "sometimes", "often"
    
    // Study habits
    @Column(name = "study_hours_start")
    private LocalTime studyHoursStart;
    
    @Column(name = "study_hours_end")
    private LocalTime studyHoursEnd;
    
    @Column(name = "study_location_preference")
    private String studyLocationPreference; // "room", "common_area", "library", "flexible"
    
    // Budget preferences
    @Column(name = "budget_min")
    private Integer budgetMin;
    
    @Column(name = "budget_max")
    private Integer budgetMax;
    
    @Column(name = "utilities_included")
    private Boolean utilitiesIncluded;
    
    // Gender preference
    @Column(name = "gender_preference")
    private String genderPreference; // "same", "opposite", "no_preference"
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
}
