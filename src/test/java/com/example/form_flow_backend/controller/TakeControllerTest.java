package com.example.form_flow_backend.controller;

import com.example.form_flow_backend.DTO.TakeSurveyRequest;
import com.example.form_flow_backend.service.TakeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TakeController.class)
@Import(TakeControllerTest.SecurityTestConfig.class)
class TakeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TakeService takeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testTakeSurvey() throws Exception {
        // 构造 mock 返回数据
        Map<String, Object> mockBody = new HashMap<>();
        mockBody.put("success", true);
        mockBody.put("message", "Answers saved successfully.");

        ResponseEntity<Map<String, Object>> mockResponse = ResponseEntity.ok(mockBody);

        // mock TakeService
        when(takeService.takeSurvey(any(TakeSurveyRequest.class)))
                .thenReturn(mockResponse);

        // 构造请求体
        TakeSurveyRequest request = new TakeSurveyRequest();
        request.setSessionToken("token123");
        request.setSurveyId("1");
        request.setAnswers("{}");

        // 发送 POST 请求并验证结果
        mockMvc.perform(post("/take/take_survey")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Answers saved successfully."));
    }

    @Test
    void testGetSurveyTakeStatistics() throws Exception {
        // 构造 mock 返回数据
        Map<String, Object> mockBody = new HashMap<>();
        mockBody.put("success", true);
        mockBody.put("stats", "someStats");  // 示例: 你可改成一个列表或更复杂结构

        ResponseEntity<Map<String, Object>> mockResponse = ResponseEntity.ok(mockBody);

        // mock TakeService
        when(takeService.getSurveyTakeStatistics(any()))
                .thenReturn(mockResponse);

        // 构造请求体
        com.example.form_flow_backend.DTO.GetSurveyDetailRequest request =
                new com.example.form_flow_backend.DTO.GetSurveyDetailRequest();
        request.setSessionToken("token123");
        request.setSurveyId("1");

        // 发送 POST 请求并验证结果
        mockMvc.perform(post("/take/get_survey_stats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.stats").value("someStats")); // 与上面 mockBody 对应
    }


    /**
     * 通过 @TestConfiguration + @Import 来向测试环境注入
     * 1) Mock 的 TakeService Bean
     * 2) 一个关闭 CSRF 验证的安全配置
     */
    @TestConfiguration
    static class SecurityTestConfig {

        // 提供一个 mock 的 TakeService 以便注入 Controller
        @Bean
        public TakeService takeService() {
            return Mockito.mock(TakeService.class);
        }

        // 关闭 CSRF，避免测试时出现 403 Forbidden
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }
}


