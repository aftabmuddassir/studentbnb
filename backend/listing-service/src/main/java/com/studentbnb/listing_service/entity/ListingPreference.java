package com.studentbnb.listing_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "listing_preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListingPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    @JsonIgnore
    private Listing listing;

    @Enumerated(EnumType.STRING)
    @Column(name = "dietary_preference")
    private DietaryPreference dietaryPreference;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender_preference")
    private GenderPreference genderPreference;

    @Enumerated(EnumType.STRING)
    @Column(name = "smoking_preference")
    private SmokingPreference smokingPreference;

    @Column(name = "additional_notes", length = 500)
    private String additionalNotes;
}
