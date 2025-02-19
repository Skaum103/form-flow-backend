package com.example.form_flow_backend.controller;

import com.example.form_flow_backend.service.SurveyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/survey")
public class SurveyController {

    private final SurveyService surveyService;

    public SurveyController(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createSurvey(@RequestBody Map<String, String> requestData,
                                          Authentication authentication) {
        // 从 Authentication 拿到登录用户名
        String loggedInUsername = authentication.getName();
        // 将请求体和用户名一起传给 Service
        return surveyService.createSurvey(loggedInUsername, requestData);
    }
}
