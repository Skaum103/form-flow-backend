package com.example.form_flow_backend.service;

import com.example.form_flow_backend.model.User;
import com.example.form_flow_backend.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserManagementService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserManagementService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Returns the current session details.
     */
    public String getSessionDetails(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "User is logged in as: " + authentication.getName();
        }
        return "No active session";
    }

    /**
     * Registers a new user with encrypted password.
     */
//    public User registerUser(User user) {
//        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
//            throw new RuntimeException("Username already exists");
//        }
//
//        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
//            throw new RuntimeException("Email already exists");
//        }
//
//        user.setPassword(passwordEncoder.encode(user.getPassword())); // Encrypt password
//        return userRepository.save(user);
//    }
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

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        response.put("success", true);
        response.put("message", "User registered successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a user if they exist.
     */
    public String deleteUser(String username) {
        Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            userRepository.delete(existingUser.get());
            return "User deleted successfully";
        }
        return "User not found";
    }


    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> {
            return userRepository.findByUsername(username)
                    .map(user -> {
                        return org.springframework.security.core.userdetails.User.builder()
                                .username(user.getUsername())
                                .password(user.getPassword()) // Ensure password is hashed
                                .roles("USER")
                                .build();
                    })
                    .orElseThrow(() -> {
                        return new RuntimeException("User not found");
                    });
        };
    }
}
