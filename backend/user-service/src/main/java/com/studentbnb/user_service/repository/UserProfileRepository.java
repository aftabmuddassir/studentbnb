package com.studentbnb.user_service.repository;

import com.studentbnb.user_service.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    
    Optional<UserProfile> findByUserId(Long userId);
    
    List<UserProfile> findByUniversityName(String universityName);
    
    List<UserProfile> findByCity(String city);
    
    List<UserProfile> findByMajor(String major);
    
    @Query("SELECT up FROM UserProfile up WHERE up.graduationYear = :year")
    List<UserProfile> findByGraduationYear(@Param("year") Integer graduationYear);
    
    @Query("SELECT up FROM UserProfile up WHERE up.universityName = :university AND up.graduationYear = :year")
    List<UserProfile> findByUniversityAndGraduationYear(@Param("university") String university, @Param("year") Integer graduationYear);
    
    @Query("SELECT up FROM UserProfile up WHERE up.city = :city AND up.universityName = :university")
    List<UserProfile> findByCityAndUniversity(@Param("city") String city, @Param("university") String university);
}