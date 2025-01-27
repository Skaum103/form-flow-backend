package com.example.form_flow_backend.controller;

import com.example.form_flow_backend.model.DummyData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
@Tag(name = "Home", description = "Endpoints for home requests")
public class HomeController {

    @Operation(summary = "Greet the user")
    @ApiResponse(responseCode = "200", description = "Successful operation")
    @GetMapping("/")
    public String greet() {
        return "Hello, World!";
    }

    @Operation(summary = "Dummy - Receive a GET request")
    @ApiResponse(responseCode = "200", description = "Successful operation")
    @GetMapping("/dummy_get")
    public String receiveGet() {
        return "Received GET request!";
    }

    @Operation(summary = "Dummy - Receive a POST request")
    @ApiResponse(responseCode = "200", description = "Successful operation")
    @PostMapping("/dummy_post")
    public String receivePost(@RequestBody DummyData data) {
        return "Received POST request with data: " + data;
    }
}
