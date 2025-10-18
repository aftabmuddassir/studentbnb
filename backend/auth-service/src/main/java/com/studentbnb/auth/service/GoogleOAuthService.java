package com.studentbnb.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.studentbnb.auth.entity.User;
import com.studentbnb.auth.entity.UserRole;
import com.studentbnb.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Service
public class GoogleOAuthService {

    @Value("${google.client.id}")
    private String googleClientId;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User authenticateGoogleUser(String idTokenString) throws Exception {
        // Verify the Google ID token
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);

        if (idToken == null) {
            throw new IllegalArgumentException("Invalid Google ID token");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();

        // Get user information from Google
        String email = payload.getEmail();
        boolean emailVerified = payload.getEmailVerified();

        if (!emailVerified) {
            throw new IllegalArgumentException("Email not verified by Google");
        }

        // Check if user exists
        Optional<User> existingUser = userRepository.findByEmail(email.toLowerCase());

        if (existingUser.isPresent()) {
            // User exists, return the user
            return existingUser.get();
        } else {
            // Create new user with Google OAuth
            User newUser = new User();
            newUser.setEmail(email.toLowerCase());
            // Generate a random password for OAuth users (they won't use it)
            newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            newUser.setRole(UserRole.STUDENT); // Default role

            return userRepository.save(newUser);
        }
    }
}
