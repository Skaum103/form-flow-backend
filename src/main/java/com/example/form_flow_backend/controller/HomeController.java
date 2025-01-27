package com.example.form_flow_backend.controller;

import com.example.form_flow_backend.model.DummyData;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class HomeController {

    @GetMapping("/home")
    public String greet() {
        return "Hello, World!";
    }

    @GetMapping("/dummy_get")
    public String receiveGet() {
        return "Received GET request!";
    }

    @PostMapping("/dummy_post")
    public String receivePost(@RequestBody DummyData data) {
        return "Received POST request with data: " + data;
    }
}
