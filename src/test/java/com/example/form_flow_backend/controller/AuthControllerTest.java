package com.example.form_flow_backend.controller;

import com.example.form_flow_backend.service.UserManagementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
        properties = "spring.main.allow-bean-definition-overriding=true"
)
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserManagementService userManagementService;

    /**
     * Test configuration that defines an in-memory user.
     * This ensures that the Spring Security login process can authenticate.
     */
    @TestConfiguration
    static class TestUserConfig {
        @Bean
        @Primary
        public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
            UserDetails user = User.builder()
                    .username("user")
                    .password(passwordEncoder.encode("password"))
                    .roles("USER")
                    .build();
            return new InMemoryUserDetailsManager(user);
        }
    }

    /**
     * Tests that a POST to /auth/login with valid credentials returns the expected JSON response.
     */
    @Test
    void testLoginSuccess() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "user")
                        .param("password", "password"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.sessionToken").exists())
                .andExpect(jsonPath("$.username").exists());
    }

    /**
     * Tests that a POST to /auth/login with invalid credentials returns the expected JSON error response.
     */
    @Test
    void testLoginFailure() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "user")
                        .param("password", "wrongpassword"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

/**
     * Test for POST /auth/logout.
     */
    @Test
    void testLogout() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"success\":true,\"message\":\"Logout successful\"}"));
    }

    /**
     * Test for POST /auth/register.
     * We send a JSON payload representing a new user and expect a JSON response.
     */
    @Test
    void testRegisterUser() throws Exception {
        Map<String, Object> expectedResponse = Map.of("status", "success", "message", "User registered");
        when(userManagementService.registerUser(any(com.example.form_flow_backend.model.User.class)))
                .thenReturn(ResponseEntity.ok(expectedResponse));

        String userJson = "{\"username\":\"newUser\",\"password\":\"password\"}";

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("User registered"));
    }


    /**
     * Tests the /auth/delete endpoint in a secured context.
     * The endpoint is protected, so we use @WithMockUser to simulate an authenticated user.
     * We stub the service to return a success message, then verify that the endpoint returns the expected response.
     */
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN", "USER"})
    void testDeleteUserProtected() throws Exception {
        // Stub the service method to return "User deleted" when called with "userToDelete"
        when(userManagementService.deleteUser("userToDelete")).thenReturn("User deleted");

        // JSON payload representing a user with the username "userToDelete"
        String userJson = "{\"username\":\"userToDelete\"}";

        // Perform a POST request to the /auth/delete endpoint as an authenticated user and verify the response
        mockMvc.perform(post("/auth/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted"));
    }
}
