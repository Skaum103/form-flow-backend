package com.example.form_flow_backend.service;

import com.example.form_flow_backend.model.Survey;
import com.example.form_flow_backend.model.User;
import com.example.form_flow_backend.repository.SurveyRepository;
import com.example.form_flow_backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class SurveyService {

    private final UserRepository userRepository;
    private final SurveyRepository surveyRepository;

    public SurveyService(UserRepository userRepository,
                         SurveyRepository surveyRepository) {
        this.userRepository = userRepository;
        this.surveyRepository = surveyRepository;
    }

    public ResponseEntity<Map<String, Object>> createSurvey(String loggedInUsername, Map<String, String> requestData) {
        Map<String, Object> response = new HashMap<>();  // 用于统一封装 JSON 响应

        // 1. 查找当前登录用户
        Optional<User> userOpt = userRepository.findByUsername(loggedInUsername);
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
}
