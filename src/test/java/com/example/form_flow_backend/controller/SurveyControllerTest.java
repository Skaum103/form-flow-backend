package com.example.form_flow_backend.controller;

import com.example.form_flow_backend.service.SurveyService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SurveyController.class)
@ContextConfiguration(classes = { SurveyController.class }) // 仅加载该 Controller
class SurveyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SurveyService surveyService;

    @Test
    void createSurvey_Success() throws Exception {
        // 1. 准备Service层的返回
        Map<String, Object> serviceResponse = new HashMap<>();
        serviceResponse.put("success", true);
        serviceResponse.put("message", "Survey created successfully");
        serviceResponse.put("surveyId", 123L);

        // SurveyService.createSurvey(...) 返回 200 OK + 这个Map
        when(surveyService.createSurvey(eq("loggedInUser"), anyMap()))
                .thenReturn(org.springframework.http.ResponseEntity.ok(serviceResponse));

        // 2. 模拟调用 /survey/create
        // 由于 @WebMvcTest 不会真正走 SecurityContextHolder 中的用户，所以我们手动 mock
        // 如果你想测试真正的登录流程，需要使用 @SpringBootTest 并配合 Security
        // 这里我们模拟 authentication.getName() = "loggedInUser"
        mockMvc.perform(post("/survey/create")
                        .principal(() -> "loggedInUser") // 模拟 Principal
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"surveyName\":\"Test Survey\",\"description\":\"Just a test\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Survey created successfully"))
                .andExpect(jsonPath("$.surveyId").value(123));

        // 3. 验证 surveyService 是否被正确调用
        verify(surveyService, times(1))
                .createSurvey(eq("loggedInUser"), anyMap());
    }

    @Test
    void createSurvey_MissingSurveyName() throws Exception {
        // 1. service 返回 400
        Map<String, Object> serviceResponse = new HashMap<>();
        serviceResponse.put("success", false);
        serviceResponse.put("message", "surveyName is required");
        when(surveyService.createSurvey(eq("loggedInUser"), anyMap()))
                .thenReturn(org.springframework.http.ResponseEntity.badRequest().body(serviceResponse));

        // 2. 发起请求，但 surveyName 缺失
        mockMvc.perform(post("/survey/create")
                        .principal(() -> "loggedInUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Just a test\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("surveyName is required"));

        // 3. 验证
        verify(surveyService, times(1))
                .createSurvey(eq("loggedInUser"), anyMap());
    }
}
