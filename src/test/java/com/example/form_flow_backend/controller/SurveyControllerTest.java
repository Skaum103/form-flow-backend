package com.example.form_flow_backend.controller;

import com.example.form_flow_backend.service.SurveyService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 使用 @SpringBootTest + @AutoConfigureMockMvc + @WithMockUser
 * 来测试 Controller 层，并 Mock 掉 SurveyService。
 */
@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("removal") // 暂时忽略 MockBean 被标记为 removal 的警告
class SurveyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SurveyService surveyService;

    @Test
    @WithMockUser(username = "loggedInUser")
    void createSurvey_Success() throws Exception {
        // 1. Service层模拟返回
        Map<String, Object> serviceResponse = new HashMap<>();
        serviceResponse.put("username", "testuser");
        serviceResponse.put("success", true);
        serviceResponse.put("message", "Survey created successfully");
        serviceResponse.put("surveyId", 123L);

        // 当 serviceService.createSurvey("loggedInUser", anything...) 被调用时，返回 200 + JSON
        when(surveyService.createSurvey(anyMap()))
                .thenReturn(ResponseEntity.ok(serviceResponse));

        // 2. 用 mockMvc 模拟 HTTP POST 请求
        mockMvc.perform(post("/survey/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"surveyName\":\"Test Survey\",\"description\":\"Just a test\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Survey created successfully"))
                .andExpect(jsonPath("$.surveyId").value(123L));

        // 3. 验证service是否被正确调用
        verify(surveyService, times(1))
                .createSurvey(anyMap());
    }

    @Test
    @WithMockUser(username = "loggedInUser")
    void createSurvey_MissingSurveyName() throws Exception {
        // 1. Service层返回 400
        Map<String, Object> serviceResponse = new HashMap<>();
        serviceResponse.put("username", "testuser");
        serviceResponse.put("success", false);
        serviceResponse.put("message", "surveyName is required");

        when(surveyService.createSurvey(anyMap()))
                .thenReturn(ResponseEntity.badRequest().body(serviceResponse));

        // 2. 此时请求体里故意不传 surveyName
        mockMvc.perform(post("/survey/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Just a test\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("surveyName is required"));

        verify(surveyService, times(1))
                .createSurvey(anyMap());
    }
}
