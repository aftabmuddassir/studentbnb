package com.studentbnb.user_service.repository;

import com.studentbnb.user_service.entity.RoommatePreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoommatePreferencesRepository extends JpaRepository<RoommatePreferences, Long> {
    
    Optional<RoommatePreferences> findByUserId(Long userId);
    
    @Query("SELECT rp FROM RoommatePreferences rp WHERE rp.budgetMin <= :maxBudget AND rp.budgetMax >= :minBudget")
    List<RoommatePreferences> findByBudgetRange(@Param("minBudget") Integer minBudget, @Param("maxBudget") Integer maxBudget);
    
    @Query("SELECT rp FROM RoommatePreferences rp WHERE rp.smokingPreference = :smoking")
    List<RoommatePreferences> findBySmokingPreference(@Param("smoking") Boolean smoking);
    
    @Query("SELECT rp FROM RoommatePreferences rp WHERE rp.petsAllowed = :petsAllowed")
    List<RoommatePreferences> findByPetsAllowed(@Param("petsAllowed") Boolean petsAllowed);
    
    @Query("SELECT rp FROM RoommatePreferences rp WHERE rp.cleanlinessLevel = :level")
    List<RoommatePreferences> findByCleanlinessLevel(@Param("level") String cleanlinessLevel);
    
    //query for roommate compatibility
    @Query("SELECT rp FROM RoommatePreferences rp WHERE " +
           "rp.budgetMin <= :maxBudget AND rp.budgetMax >= :minBudget AND " +
           "rp.smokingPreference = :smoking AND " +
           "rp.petsAllowed = :petsOk AND " +
           "rp.user.id != :userId")
    List<RoommatePreferences> findCompatibleRoommates(
        @Param("userId") Long userId,
        @Param("minBudget") Integer minBudget, 
        @Param("maxBudget") Integer maxBudget,
        @Param("smoking") Boolean smoking,
        @Param("petsOk") Boolean petsOk
    );
}