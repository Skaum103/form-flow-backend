package com.example.form_flow_backend.controller;

import com.example.form_flow_backend.DTO.UpdateQuestionsRequest;
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
    public ResponseEntity<?> createSurvey(@RequestBody Map<String, String> requestData) {
        // 将请求体和用户名一起传给 Service
        return surveyService.createSurvey(requestData);
    }

    @PostMapping("/update_questions")
    public ResponseEntity<?> updateQuestions(@RequestBody UpdateQuestionsRequest request) {
        return surveyService.updateQuestions(request);
    }
}
