package com.example.form_flow_backend.controller;

import com.example.form_flow_backend.DTO.CreateSurveyRequest;
import com.example.form_flow_backend.DTO.UpdateQuestionsRequest;
import com.example.form_flow_backend.service.SessionService;
import com.example.form_flow_backend.service.SurveyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/survey")
public class SurveyController {

    private final SurveyService surveyService;
    private final SessionService sessionService;

    public SurveyController(SurveyService surveyService, SessionService sessionService) {
        this.surveyService = surveyService;
        this.sessionService = sessionService;
    }

    /**
     * 创建问卷
     * 前端以 JSON 格式传入 { "sessionToken", "surveyName", "description" }
     */
    @PostMapping("/create")
    public ResponseEntity<?> createSurvey(@RequestBody CreateSurveyRequest request) {

        return surveyService.createSurvey(request);
    }

    /**
     * 更新问卷问题
     */
    @PostMapping("/update_questions")
    public ResponseEntity<?> updateQuestions(@RequestBody UpdateQuestionsRequest request) {
        // 这里仍在 Controller 中做 sessionToken 的初步校验，你也可以选择把验证逻辑都放到 Service。
        if (request.getSessionToken() == null || !sessionService.verifySession(request.getSessionToken())) {
            return ResponseEntity.badRequest().body("Unauthorized");
        }
        return surveyService.updateQuestions(request);
    }
}
