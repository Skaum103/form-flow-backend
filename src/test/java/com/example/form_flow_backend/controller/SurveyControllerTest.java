package com.example.form_flow_backend.controller;

import com.example.form_flow_backend.DTO.CreateSurveyRequest;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
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
        // 1. Service 层模拟返回
        Map<String, Object> serviceResponse = new HashMap<>();
        serviceResponse.put("success", true);
        serviceResponse.put("message", "Survey created successfully");
        serviceResponse.put("surveyId", 123L);

        // 当 surveyService.createSurvey(...) 被调用时，返回 200 + JSON
        when(surveyService.createSurvey(any(CreateSurveyRequest.class)))
                .thenReturn(ResponseEntity.ok(serviceResponse));

        // 2. 用 mockMvc 模拟 HTTP POST 请求
        //   注意，这里在请求体中包含了 sessionToken, surveyName, description
        mockMvc.perform(post("/survey/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionToken\":\"test-token\",\"surveyName\":\"Test Survey\",\"description\":\"Just a test\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Survey created successfully"))
                .andExpect(jsonPath("$.surveyId").value(123L));

        // 3. 验证 service 是否被正确调用
        verify(surveyService, times(1))
                .createSurvey(any(CreateSurveyRequest.class));
    }

    @Test
    @WithMockUser(username = "loggedInUser")
    void createSurvey_MissingSurveyName() throws Exception {
        // 1. Service 层返回 400
        Map<String, Object> serviceResponse = new HashMap<>();
        serviceResponse.put("success", false);
        serviceResponse.put("message", "Survey name is required.");

        when(surveyService.createSurvey(any(CreateSurveyRequest.class)))
                .thenReturn(ResponseEntity.badRequest().body(serviceResponse));

        // 2. 此时请求体里故意不传 surveyName，只传 sessionToken 与 description
        mockMvc.perform(post("/survey/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionToken\":\"test-token\",\"description\":\"Just a test\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Survey name is required."));

        verify(surveyService, times(1))
                .createSurvey(any(CreateSurveyRequest.class));
    }
}
