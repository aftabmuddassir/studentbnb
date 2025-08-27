package com.studentbnb.user_service.repository;

import com.studentbnb.user_service.entity.User;
import com.studentbnb.user_service.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User student1;
    private User student2;
    private User landlord1;

    @BeforeEach
    void setUp() {
        // Create test users
        student1 = new User(1L, "student1@university.edu", UserRole.STUDENT);
        student2 = new User(2L, "student2@university.edu", UserRole.STUDENT);
        landlord1 = new User(3L, "landlord1@example.com", UserRole.LANDLORD);

        // Set verification status
        student1.setIsVerified(true);
        student1.setIsActive(true);
        
        student2.setIsVerified(false);
        student2.setIsActive(true);
        
        landlord1.setIsVerified(true);
        landlord1.setIsActive(true);

        // Save to database
        userRepository.save(student1);
        userRepository.save(student2);
        userRepository.save(landlord1);
    }

    @Test
    void findByEmail_Success() {
        Optional<User> found = userRepository.findByEmail("student1@university.edu");
        
        assertTrue(found.isPresent());
        assertEquals("student1@university.edu", found.get().getEmail());
        assertEquals(UserRole.STUDENT, found.get().getRole());
    }

    @Test
    void findByEmail_NotFound() {
        Optional<User> found = userRepository.findByEmail("notfound@example.com");
        
        assertFalse(found.isPresent());
    }

    @Test
    void existsByEmail_True() {
        boolean exists = userRepository.existsByEmail("student1@university.edu");
        
        assertTrue(exists);
    }

    @Test
    void existsByEmail_False() {
        boolean exists = userRepository.existsByEmail("notfound@example.com");
        
        assertFalse(exists);
    }

    @Test
    void findByRole_Students() {
        List<User> students = userRepository.findByRole(UserRole.STUDENT);
        
        assertEquals(2, students.size());
        assertTrue(students.stream().allMatch(user -> user.getRole() == UserRole.STUDENT));
    }

    @Test
    void findByRole_Landlords() {
        List<User> landlords = userRepository.findByRole(UserRole.LANDLORD);
        
        assertEquals(1, landlords.size());
        assertEquals(UserRole.LANDLORD, landlords.get(0).getRole());
    }

    @Test
    void findByIsVerified_True() {
        List<User> verifiedUsers = userRepository.findByIsVerified(true);
        
        assertEquals(2, verifiedUsers.size());
        assertTrue(verifiedUsers.stream().allMatch(User::getIsVerified));
    }

    @Test
    void findByIsVerified_False() {
        List<User> unverifiedUsers = userRepository.findByIsVerified(false);
        
        assertEquals(1, unverifiedUsers.size());
        assertFalse(unverifiedUsers.get(0).getIsVerified());
    }

    @Test
    void findActiveVerifiedUsersByRole_Students() {
        List<User> activeVerifiedStudents = userRepository.findActiveVerifiedUsersByRole(UserRole.STUDENT);
        
        assertEquals(1, activeVerifiedStudents.size());
        User user = activeVerifiedStudents.get(0);
        assertEquals(UserRole.STUDENT, user.getRole());
        assertTrue(user.getIsVerified());
        assertTrue(user.getIsActive());
    }

    @Test
    void findActiveUsers() {
        List<User> activeUsers = userRepository.findActiveUsers();
        
        assertEquals(3, activeUsers.size());
        assertTrue(activeUsers.stream().allMatch(User::getIsActive));
    }

    @Test
    void findActiveUsers_WithInactiveUser() {
        // Create and save an inactive user
        User inactiveUser = new User(4L, "inactive@example.com", UserRole.STUDENT);
        inactiveUser.setIsActive(false);
        userRepository.save(inactiveUser);

        List<User> activeUsers = userRepository.findActiveUsers();
        
        assertEquals(3, activeUsers.size());
        assertTrue(activeUsers.stream().allMatch(User::getIsActive));
        assertFalse(activeUsers.stream().anyMatch(user -> 
            user.getEmail().equals("inactive@example.com")));
    }
}