package com.example.form_flow_backend.controller;

import com.example.form_flow_backend.model.User;
import com.example.form_flow_backend.service.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserManagementService userManagementService;

    // Constructor injection for UserManagementService
    public AuthController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    /**
     * Login endpoint.
     * This method handles login requests but currently returns null.
     */
    @PostMapping("/login")
    @Operation(summary = "Login the user")
    @ApiResponse(responseCode = "200", description = "Login success, will also return a session cookie")
    @ApiResponse(responseCode = "401", description = "Credentials not correct")
    public String login(@RequestBody String username, @RequestBody String password) {
        return "Session";
    }

    /**
     * Logout endpoint.
     * This method handles logout requests but currently returns null.
     */
    @PostMapping("/logout")
    @Operation(summary = "Login the user")
    @ApiResponse(responseCode = "200", description = "Logout success, will deicard the session")
    public String logout() {
        return "Logout success";
    }

    /**
     * Registers a new user.
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(@RequestBody User user) {
        return userManagementService.registerUser(user);
    }

    /**
     * Deletes a user by username.
     * Deletes the user using the provided username.
     */
    @PostMapping("/delete")
    public String deleteUser(@RequestBody User user) {
        return userManagementService.deleteUser(user.getUsername());
    }
}
