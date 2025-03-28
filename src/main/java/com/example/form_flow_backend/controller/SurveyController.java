package com.example.form_flow_backend.controller;

import com.example.form_flow_backend.DTO.CreateSurveyRequest;
import com.example.form_flow_backend.DTO.GetSurveyDetailRequest;
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

    public SurveyController(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createSurvey(@RequestBody CreateSurveyRequest request) {
        return surveyService.createSurvey(request);
    }

    @PostMapping("/update_questions")
    public ResponseEntity<?> updateQuestions(@RequestBody UpdateQuestionsRequest request) {
        return surveyService.updateQuestions(request);
    }

    @PostMapping("/getSurvey")
    public ResponseEntity<?> getSurvey(@RequestBody Map<String, String> request) {
        return surveyService.getAllSurveysForUser(request.get("sessionToken"));
    }

    @PostMapping("/get_survey_detail")
    public ResponseEntity<?> getSurveyDetail(@RequestBody GetSurveyDetailRequest request) {
        return surveyService.getSurveyDetail(request);
    }

    @PostMapping("/get_accessible_survey")
    public ResponseEntity<?> getAccessibleSurvey(@RequestBody Map<String, String> request) {
        return surveyService.getAccessibleSurvey(request.get("sessionToken"));
    }
}
