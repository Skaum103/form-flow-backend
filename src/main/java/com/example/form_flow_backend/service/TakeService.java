package com.example.form_flow_backend.service;

import com.example.form_flow_backend.DTO.TakeSurveyRequest;
import com.example.form_flow_backend.model.Session;
import com.example.form_flow_backend.model.Survey;
import com.example.form_flow_backend.model.Takes;
import com.example.form_flow_backend.model.User;
import com.example.form_flow_backend.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Service
public class TakeService {
    private final TakesRepository takesRepository;
    private final SessionRepository sessionRepository;
    private final SessionService sessionService;
    private final UserRepository userRepository;
    private final SurveyRepository surveyRepository;

    public TakeService(TakesRepository takesRepository, SessionRepository sessionRepository, SessionService sessionService, UserRepository userRepository, SurveyRepository surveyRepository) {
        this.takesRepository = takesRepository;
        this.sessionRepository = sessionRepository;
        this.sessionService = sessionService;
        this.userRepository = userRepository;
        this.surveyRepository = surveyRepository;
    }

    public ResponseEntity<Map<String, Object>> takeSurvey(TakeSurveyRequest request) {
        Map<String, Object> response = new HashMap<>();

        // 1. 校验 sessionToken 是否为空
        String sessionToken = request.getSessionToken();
        if (sessionToken == null) {
            response.put("success", false);
            response.put("message", "Session token is missing.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        if (!sessionService.verifySession(sessionToken)) {
            response.put("success", false);
            response.put("message", "Unauthorized or session expired.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        Optional<Session> sessionOpt = sessionRepository.findBySessionToken(sessionToken);
        Session session = sessionOpt.get();

        // 3. 获取 username，并查询 User
        String username = sessionOpt.get().getUsername();
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "User not found in database.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        User user = userOpt.get();

        // 3. 查找并校验 Survey
        Long surveyId;
        try {
            surveyId = Long.valueOf(request.getSurveyId());
        } catch (NumberFormatException e) {
            response.put("success", false);
            response.put("message", "Invalid survey ID.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        Optional<Survey> surveyOpt = surveyRepository.findById(surveyId);
        if (surveyOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Survey not found in database.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        Survey survey = surveyOpt.get();

        Takes take = new Takes();
        take.setUser(user);
        take.setSurvey(survey);
        take.setAnswers(request.getAnswers());

        takesRepository.save(take);
        response.put("success", true);
        response.put("message", "Answers saved successfully.");

        return ResponseEntity.ok(response);
    }
}
