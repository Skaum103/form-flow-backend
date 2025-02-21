package com.example.form_flow_backend.service;

import com.example.form_flow_backend.DTO.CreateSurveyRequest;
import com.example.form_flow_backend.DTO.UpdateQuestionsRequest;
import com.example.form_flow_backend.model.Survey;
import com.example.form_flow_backend.model.User;
import com.example.form_flow_backend.model.Question;
import com.example.form_flow_backend.repository.QuestionRepository;
import com.example.form_flow_backend.repository.SurveyRepository;
import com.example.form_flow_backend.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SurveyService {

    private final UserRepository userRepository;
    private final SurveyRepository surveyRepository;
    private final QuestionRepository questionRepository;

    public SurveyService(UserRepository userRepository,
                         SurveyRepository surveyRepository, QuestionRepository questionRepository) {
        this.userRepository = userRepository;
        this.surveyRepository = surveyRepository;
        this.questionRepository = questionRepository;
    }

    public ResponseEntity<Map<String, Object>> createSurvey(CreateSurveyRequest request) {
        Map<String, Object> response = new HashMap<>();  // 用于统一封装 JSON 响应

        // 1. 查找当前登录用户
        String username = requestData.get("username");
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "User not found in database");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        User user = userOpt.get();

        // 2. 获取必要字段
        String surveyName = requestData.get("surveyName");
        if (surveyName == null || surveyName.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "surveyName is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        String description = requestData.get("description");

        // 3. 构造 Survey 对象
        Survey newSurvey = new Survey();
        newSurvey.setSurveyName(surveyName);
        newSurvey.setDescription(description);
        newSurvey.setUser(user);

        // 4. 保存到数据库
        Survey savedSurvey = surveyRepository.save(newSurvey);

        // 5. 返回成功 JSON
        response.put("success", true);
        response.put("message", "Survey created successfully");
        response.put("surveyId", savedSurvey.getId());
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String, Object>> updateQuestions(UpdateQuestionsRequest request) {
        Map<String, Object> response = new HashMap<>();  // 用于统一封装 JSON 响应

        // 1. 查找当前 survey
        Long surveyId = Long.valueOf(request.getSurveyId());
        Optional<Survey> surveyOpt = surveyRepository.findById(surveyId);
        if (surveyOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Survey not found in database");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        Survey survey = surveyOpt.get();

        // 3. 清空现有问题（假设这里是完全替换）
        questionRepository.deleteBySurveyId(surveyId);

        List<Question> questionList = request.getQuestions();
        for (Question question : questionList) {
            question.setSurvey(survey);
        }
        questionRepository.saveAll(questionList);

        // 6. 构造响应
        response.put("success", true);
        response.put("message", "Questions updated successfully");
        response.put("data", survey);

        return ResponseEntity.ok(response);
    }

}
