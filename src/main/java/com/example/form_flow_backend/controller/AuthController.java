package com.example.form_flow_backend.controller;

import com.example.form_flow_backend.model.User;
import com.example.form_flow_backend.service.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
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
    

    @GetMapping("/login")
    @Operation(summary = "Login the user")
    @ApiResponse(responseCode = "200", description = "Login success, will also return a session cookie")
    @ApiResponse(responseCode = "403", description = "Credentials not correct")
    public Object login(@RequestBody String username, @RequestBody String password) {
        return null;
    }

    /**
     * Registers a new user.
     */
//    @PostMapping("/register")
//    public User registerUser(@RequestBody User user) {
//        try {
//            return userManagementService.registerUser(user);
//        } catch (RuntimeException e) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
//        }
//    }
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(@RequestBody User user) {
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
