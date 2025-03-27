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

    public TakeService(
            TakesRepository takesRepository,
            SessionRepository sessionRepository,
            SessionService sessionService,
            UserRepository userRepository,
            SurveyRepository surveyRepository
    ) {
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

        // 2. 验证 sessionToken 是否有效
        if (!sessionService.verifySession(sessionToken)) {
            response.put("success", false);
            response.put("message", "Unauthorized or session expired.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // 3. 在数据库中找对应的 Session
        Optional<Session> sessionOpt = sessionRepository.findBySessionToken(sessionToken);
        if (sessionOpt.isEmpty()) {
            // 如果在数据库里也没找到这条 session 记录，就返回 401
            response.put("success", false);
            response.put("message", "Unauthorized or session expired. (Session not found in DB)");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        Session session = sessionOpt.get();

        // 4. 根据 username 找到对应的用户
        String username = session.getUsername();
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "User not found in database.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        User user = userOpt.get();

        // 5. 校验 surveyId
        Long surveyId;
        try {
            surveyId = Long.valueOf(request.getSurveyId());
        } catch (NumberFormatException e) {
            response.put("success", false);
            response.put("message", "Invalid survey ID.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // 6. 查询 Survey
        Optional<Survey> surveyOpt = surveyRepository.findById(surveyId);
        if (surveyOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Survey not found in database.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        Survey survey = surveyOpt.get();

        // 7. 保存 Takes
        Takes take = new Takes();
        take.setUser(user);
        take.setSurvey(survey);
        take.setAnswers(request.getAnswers());

        takesRepository.save(take);

        // 8. 返回成功响应
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

        // 2. 验证 sessionToken 是否有效
        if (!sessionService.verifySession(sessionToken)) {
            response.put("success", false);
            response.put("message", "Unauthorized or session expired.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // 3. 在数据库中找对应的 Session
        Optional<Session> sessionOpt = sessionRepository.findBySessionToken(sessionToken);
        if (sessionOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Unauthorized or session expired. (Session not found in DB)");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        Session session = sessionOpt.get();

        // 4. 校验 SurveyId
        Long surveyId;
        try {
            surveyId = Long.valueOf(request.getSurveyId());
        } catch (NumberFormatException e) {
            response.put("success", false);
            response.put("message", "Invalid survey ID.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // 5. 查询 Takes
        List<Takes> takes = takesRepository.findTakesBySurveyId(surveyId);
        if (takes.isEmpty()) {
            response.put("success", false);
            response.put("message", "No takes found for this survey.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // 6. 计算回答的数量分布
        Takes takePeek = takes.get(0); // 改成 get(0) 而不是 getFirst()
        String[] peekSplit = takePeek.getAnswers().split(";");
        int surveyLen = peekSplit.length;

        ArrayList<TakesStatsDTO> takesStatsDTOS = new ArrayList<>();
        for (int i = 0; i < surveyLen; i++) {
            TakesStatsDTO takesStatsDTO = new TakesStatsDTO();
            takesStatsDTO.setQuestion_order(i + 1);
            takesStatsDTO.setStats(new HashMap<>());
            takesStatsDTOS.add(takesStatsDTO);
        }

        // 7. 填充统计
        for (Takes singleTake : takes) {
            String[] answers = singleTake.getAnswers().split(";");
            for (int i = 0; i < surveyLen; i++) {
                String[] answerParts = answers[i].split(",");
                TakesStatsDTO dto = takesStatsDTOS.get(i);

                for (String ans : answerParts) {
                    dto.getStats().put(
                            ans,
                            dto.getStats().getOrDefault(ans, 0) + 1
                    );
                }
            }
        }

        // 8. 返回结果
        response.put("success", true);
        response.put("stats", takesStatsDTOS);
        return ResponseEntity.ok(response);
    }
}
