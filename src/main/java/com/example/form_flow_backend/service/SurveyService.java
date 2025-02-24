package com.example.form_flow_backend.service;

import com.example.form_flow_backend.DTO.CreateSurveyRequest;
import com.example.form_flow_backend.DTO.UpdateQuestionsRequest;
import com.example.form_flow_backend.model.Question;
import com.example.form_flow_backend.model.Session;
import com.example.form_flow_backend.model.Survey;
import com.example.form_flow_backend.model.User;
import com.example.form_flow_backend.repository.QuestionRepository;
import com.example.form_flow_backend.repository.SessionRepository;
import com.example.form_flow_backend.repository.SurveyRepository;
import com.example.form_flow_backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SurveyService {

    private final UserRepository userRepository;
    private final SurveyRepository surveyRepository;
    private final QuestionRepository questionRepository;
    private final SessionRepository sessionRepository;

    public SurveyService(
            UserRepository userRepository,
            SurveyRepository surveyRepository,
            QuestionRepository questionRepository,
            SessionRepository sessionRepository
    ) {
        this.userRepository = userRepository;
        this.surveyRepository = surveyRepository;
        this.questionRepository = questionRepository;
        this.sessionRepository = sessionRepository;
    }

    /**
     * 创建问卷
     */
    public ResponseEntity<Map<String, Object>> createSurvey(CreateSurveyRequest request) {
        Map<String, Object> response = new HashMap<>();

        // 1. 校验 sessionToken 是否为空
        if (request.getSessionToken() == null) {
            response.put("success", false);
            response.put("message", "Session token is missing.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // 2. 根据 sessionToken 查找 Session
        Optional<Session> sessionOpt = sessionRepository.findBySessionToken(request.getSessionToken());
        if (sessionOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Session not found or invalid.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // 3. 获取 username，并查询 User
        String username = sessionOpt.get().getUsername();
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "User not found in database.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        User user = userOpt.get();

        // 4. 校验必填字段 surveyName
        if (request.getSurveyName() == null || request.getSurveyName().trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Survey name is required.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // 5. 构造 Survey 对象
        Survey newSurvey = new Survey();
        newSurvey.setSurveyName(request.getSurveyName());
        newSurvey.setDescription(request.getDescription());
        newSurvey.setUser(user);

        // 6. 保存到数据库
        Survey savedSurvey = surveyRepository.save(newSurvey);

        // 7. 构造返回响应
        response.put("success", true);
        response.put("message", "Survey created successfully.");
        response.put("surveyId", savedSurvey.getId());

        return ResponseEntity.ok(response);
    }

    /**
     * 更新问题列表
     */
    public ResponseEntity<Map<String, Object>> updateQuestions(UpdateQuestionsRequest request) {
        Map<String, Object> response = new HashMap<>();

        // 1. 校验 sessionToken 是否为空
        if (request.getSessionToken() == null) {
            response.put("success", false);
            response.put("message", "Session token is missing.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // 2. 根据 sessionToken 查找 Session
        Optional<Session> sessionOpt = sessionRepository.findBySessionToken(request.getSessionToken());
        if (sessionOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Session not found or invalid.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

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

        // 4. 先删除原有问题（假设是“整表替换”模式）
        questionRepository.deleteBySurveyId(surveyId);

        // 5. 为新问题设置所属 survey 并批量保存
        List<Question> questionList = request.getQuestions();
        if (questionList != null) {
            for (Question question : questionList) {
                question.setSurvey(survey);
            }
            questionRepository.saveAll(questionList);
        }

        // 6. 返回成功结果
        response.put("success", true);
        response.put("message", "Questions updated successfully.");

        return ResponseEntity.ok(response);
    }
}
