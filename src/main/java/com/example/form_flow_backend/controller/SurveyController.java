package com.example.form_flow_backend.controller;

import com.example.form_flow_backend.DTO.CreateSurveyRequest;
import com.example.form_flow_backend.DTO.UpdateQuestionsRequest;
import com.example.form_flow_backend.service.SessionService;
import com.example.form_flow_backend.service.SurveyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/survey")
public class SurveyController {

    private final SurveyService surveyService;
    private final SessionService sessionService;

    public SurveyController(SurveyService surveyService, SessionService sessionService) {
        this.surveyService = surveyService;
        this.sessionService = sessionService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createSurvey(@RequestBody CreateSurveyRequest request) {

        return surveyService.createSurvey(request);
    }

    @PostMapping("/update_questions")
    public ResponseEntity<?> updateQuestions(@RequestBody UpdateQuestionsRequest request) {
        if (request.getSessionToken() == null || !sessionService.verifySession(request.getSessionToken())) {
            return ResponseEntity.badRequest().body("Unauthorized");
        }
        return surveyService.updateQuestions(request);
    }

    @PostMapping("/getSurvey")
    public ResponseEntity<?> getSurvey(@RequestBody Map<String, String> requestBody) {
        String sessionToken = requestBody.get("sessionToken");

        if (sessionToken == null || sessionToken.isEmpty()) {
            return ResponseEntity.badRequest().body("Session token is missing.");
        }
        if (!sessionService.verifySession(sessionToken)) {
            return ResponseEntity.status(401).body("Unauthorized or session expired.");
        }

        return surveyService.getAllSurveysForUser(sessionToken);
    }
}
