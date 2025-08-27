package com.studentbnb.user_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentbnb.user_service.config.SecurityConfig;
import com.studentbnb.user_service.dto.CreateUserProfileRequest;
import com.studentbnb.user_service.entity.User;
import com.studentbnb.user_service.entity.UserRole;
import com.studentbnb.user_service.repository.UserRepository;
import com.studentbnb.user_service.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private User testUser;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        // Clear any existing data
    userRepository.deleteAll();

    
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Create test user
        testUser = new User(1L, "test@university.edu", UserRole.STUDENT);
        testUser = userRepository.save(testUser);

        // Generate JWT token for testing
        jwtToken = generateTestToken(testUser);
    }

    @Test
    void healthCheck_Success() throws Exception {
        mockMvc.perform(get("/api/users/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("User Service is running"))
                .andExpect(jsonPath("$.service").value("user-service"));
    }

    @Test
    void createUserProfile_Success() throws Exception {
        CreateUserProfileRequest request = createValidProfileRequest();

        mockMvc.perform(post("/api/users/profile")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("test@university.edu"));
    }

    @Test
    void createUserProfile_WithoutAuth_Unauthorized() throws Exception {
        CreateUserProfileRequest request = createValidProfileRequest();

        mockMvc.perform(post("/api/users/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createUserProfile_InvalidData_BadRequest() throws Exception {
        CreateUserProfileRequest request = new CreateUserProfileRequest();
        // Missing required fields

        mockMvc.perform(post("/api/users/profile")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

   @Test
void getCurrentUserProfile_NoProfile_NotFound() throws Exception {
    // Create a user without a profile
    User userWithoutProfile = new User(999L, "noprofile@test.edu", UserRole.STUDENT);
    userWithoutProfile = userRepository.save(userWithoutProfile);
    String tokenWithoutProfile = generateTestToken(userWithoutProfile);

    mockMvc.perform(get("/api/users/profile")
            .header("Authorization", "Bearer " + tokenWithoutProfile))
            .andExpect(status().isNotFound());
}

    @Test
    void getUserProfile_Success() throws Exception {
        // First create a profile
        CreateUserProfileRequest request = createValidProfileRequest();
        
        mockMvc.perform(post("/api/users/profile")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Then get it
        mockMvc.perform(get("/api/users/profile")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void searchUsersByRole_Success() throws Exception {
        mockMvc.perform(get("/api/users/search/role/STUDENT")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void searchUsersByRole_InvalidRole_BadRequest() throws Exception {
        mockMvc.perform(get("/api/users/search/role/INVALID_ROLE")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid role: INVALID_ROLE"));
    }

    @Test
    void createUser_ServiceEndpoint_Success() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("userId", 999L);
        request.put("email", "newuser@university.edu");
        request.put("role", "STUDENT");

        mockMvc.perform(post("/api/users/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User created successfully"))
                .andExpect(jsonPath("$.userId").value(999));
    }

    // Helper method to create valid profile request
    private CreateUserProfileRequest createValidProfileRequest() {
        CreateUserProfileRequest request = new CreateUserProfileRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPhoneNumber("+1234567890");
        request.setDateOfBirth(LocalDate.of(2000, 1, 15));
        request.setBio("Test bio");
        request.setUniversityName("Test University");
        request.setMajor("Computer Science");
        request.setGraduationYear(2025);
        request.setAddress("123 Test St");
        request.setCity("Test City");
        request.setState("CA");
        request.setZipCode("90210");
        return request;
    }

    // Helper method to generate test JWT token
    private String generateTestToken(User user) {
        // Create claims for the token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole().toString());
        
        return jwtService.generateToken(claims, user);
    }
}