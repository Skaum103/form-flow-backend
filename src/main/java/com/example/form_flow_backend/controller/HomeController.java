package com.example.form_flow_backend.controller;

import com.example.form_flow_backend.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * HomeController handles requests to the home endpoints.
 */
@RestController
@RequestMapping("/")
@Tag(name = "Home", description = "Endpoints for home requests")
public class HomeController {

    /**
     * Greet the user.
     * English comment: Returns a simple greeting message.
     *
     * @return greeting message
     */
    @Operation(summary = "Greet the user")
    @ApiResponse(responseCode = "200", description = "Successful operation")
    @GetMapping("/home")
    public String greet() {
        return "Hello, World!";
    }

    /**
     * Receives a GET request.
     * English comment: Dummy endpoint for GET request testing.
     *
     * @return a message indicating a GET request was received
     */
    @Operation(summary = "Dummy - Receive a GET request")
    @ApiResponse(responseCode = "200", description = "Successful operation")
    @ApiResponse(responseCode = "404", description = "Resource not found")
    @GetMapping("/dummy_get")
    public String receiveGet() {
        return "Received GET request!";
    }

    /**
     * Receives a POST request with dummy data.
     * English comment: Dummy endpoint for POST request testing.
     *
     * @param data the dummy data sent in the request body
     * @return a message indicating the POST request was received along with data
     */
    @Operation(summary = "Dummy - Receive a POST request")
    @ApiResponse(responseCode = "200", description = "Successful operation")
    @ApiResponse(responseCode = "404", description = "Resource not found")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @PostMapping("/dummy_post")
    public String receivePost(@RequestBody User data) {
        return "Received POST request with data: " + data;
    }
}
