package com.example.form_flow_backend.service;

import com.example.form_flow_backend.model.User;
import com.example.form_flow_backend.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service for managing user operations.
 * Provides methods for user session handling, registration, and deletion.
 */
@Service
public class UserManagementService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor for dependency injection.
     *
     * @param userRepository  the repository for user data
     * @param passwordEncoder the encoder for user passwords
     */
    public UserManagementService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Returns the current session details.
     * Retrieves session details based on the current authentication context.
     *
     * @param authentication the current authentication object
     * @return session details as a String
     */
    public String getSessionDetails(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "User is logged in as: " + authentication.getName();
        }
        return "No active session";
    }

    /**
     * Registers a new user.
     * Validates and registers a user if the username and email are not already in use.
     *
     * @param user the user to register
     * @return a ResponseEntity containing the result of the registration
     */
    public ResponseEntity<Map<String, Object>> registerUser(User user) {
        Map<String, Object> response = new HashMap<>();

        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            response.put("success", false);
            response.put("message", "Username already exists");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            response.put("success", false);
            response.put("message", "Email already exists");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Encrypt password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        response.put("success", true);
        response.put("message", "User registered successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a user if they exist.
     * Removes a user identified by username.
     *
     * @param username the username of the user to delete
     * @return a message indicating the result of the deletion
     */
    public String deleteUser(String username) {
        Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            userRepository.delete(existingUser.get());
            return "User deleted successfully";
        }
        return "User not found";
    }

    /**
     * Configures the UserDetailsService bean.
     * Provides a UserDetailsService for authentication using user repository.
     *
     * @param userRepository the repository for user data
     * @return a UserDetailsService instance
     */
    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByUsername(username)
                .map(user -> org.springframework.security.core.userdetails.User.builder()
                        .username(user.getUsername())
                        .password(user.getPassword()) // BCrypt hashed password
                        .roles("USER")
                        .build()
                )
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
