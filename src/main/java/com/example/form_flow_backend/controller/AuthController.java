package com.example.form_flow_backend.controller;

import com.example.form_flow_backend.model.User;
import com.example.form_flow_backend.service.UserManagementService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserManagementService userManagementService;

    public AuthController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    /**
     * Retrieves session details.
     */
    @GetMapping("/session")
    public String getSession(Authentication authentication) {
        return userManagementService.getSessionDetails(authentication);
    }

    /**
     * Registers a new user.
     */
    @PostMapping("/register")
    public User registerUser(@RequestBody User user) {
        return userManagementService.registerUser(user);
    }

    /**
     * Deletes a user by username.
     */
    @PostMapping("/delete")
    public String deleteUser(@RequestBody User user) {
        return userManagementService.deleteUser(user.getUsername());
    }
}
