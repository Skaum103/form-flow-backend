package com.example.form_flow_backend.service;

import com.example.form_flow_backend.DTO.GetSurveyDetailRequest;
import com.example.form_flow_backend.DTO.TakeSurveyRequest;
import com.example.form_flow_backend.DTO.TakesStatsDTO;
import com.example.form_flow_backend.model.Session;
import com.example.form_flow_backend.model.Survey;
import com.example.form_flow_backend.model.Takes;
import com.example.form_flow_backend.model.User;
import com.example.form_flow_backend.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;


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

    public ResponseEntity<Map<String, Object>> getSurveyTakeStatistics(GetSurveyDetailRequest request) {
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

        // 2. 查找并校验 Survey
        Long surveyId;
        try {
            surveyId = Long.valueOf(request.getSurveyId());
        } catch (NumberFormatException e) {
            response.put("success", false);
            response.put("message", "Invalid survey ID.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // 3. Parse Takes
        List<Takes> takes = takesRepository.findTakesBySurveyId(surveyId);
        Takes takePeek = takes.getFirst();
        int surveyLen = takePeek.getAnswers().split(";").length;
        ArrayList<TakesStatsDTO> takesStatsDTOS = new ArrayList<>();

        for (int i = 0; i < surveyLen; i++) {
            TakesStatsDTO takesStatsDTO = new TakesStatsDTO();
            takesStatsDTO.setQuestion_order(i);
            HashMap<String, Integer> takesStats = new HashMap<>();
            takesStatsDTO.setStats(takesStats);
            takesStatsDTOS.add(takesStatsDTO);
        }

        for (Takes take : takes) {
            String[] answers = take.getAnswers().split(";");
            for (int i = 0; i < surveyLen; i++) {
                String[] answer = answers[i].split(",");
                TakesStatsDTO takesStatsDTO = takesStatsDTOS.get(i);
                HashMap<String, Integer> takesStats = takesStatsDTO.getStats();
                if (answer.length == 1) {
                    takesStats.put(answer[0], takesStats.getOrDefault(answer[0], 0) + 1);
                } else {
                    for (String ans : answer) {
                        takesStats.put(ans, takesStats.getOrDefault(ans, 0) + 1);
                    }
                }
            }
        }

        response.put("stats", takesStatsDTOS);
        return ResponseEntity.ok(response);
    }
}
