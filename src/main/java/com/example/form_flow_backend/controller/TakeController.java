package com.example.form_flow_backend.controller;

import com.example.form_flow_backend.DTO.GetSurveyDetailRequest;
import com.example.form_flow_backend.DTO.TakeSurveyRequest;
import com.example.form_flow_backend.service.TakeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/take")
public class TakeController {

    private final TakeService takeService;

    public TakeController(TakeService takeService) {
        this.takeService = takeService;
    }

    @PostMapping("/take_survey")
    public ResponseEntity<?> takeSurvey(@RequestBody TakeSurveyRequest request) {
        return takeService.takeSurvey(request);
    }
}
