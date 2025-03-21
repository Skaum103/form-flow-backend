package com.example.form_flow_backend.controller;

import com.example.form_flow_backend.DTO.CreateSurveyRequest;
import com.example.form_flow_backend.DTO.GetSurveyDetailRequest;
import com.example.form_flow_backend.DTO.UpdateQuestionsRequest;
import com.example.form_flow_backend.service.SessionService;
import com.example.form_flow_backend.service.SurveyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SurveyController.class)
@AutoConfigureMockMvc(addFilters = false)
public class SurveyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SurveyService surveyService;


    @Test
    @WithMockUser(username = "testUser")
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
    @WithMockUser(username = "testUser")
    public void testUpdateQuestions_Success() throws Exception {
        // Prepare a mocked service response.
        Map<String, Object> serviceResponse = Map.of(
                "success", true,
                "message", "Questions updated successfully."
        );
        when(surveyService.updateQuestions(any(UpdateQuestionsRequest.class)))
                .thenReturn(ResponseEntity.ok(serviceResponse));

        // Prepare JSON request payload.
        String jsonRequest = "{\"sessionToken\":\"valid-token\",\"surveyId\":\"1\",\"questions\":["
                + "{\"description\":\"q1\",\"body\":\"q1body\"},"
                + "{\"description\":\"q2\",\"body\":\"q2body\"}"
                + "]}";

        mockMvc.perform(post("/survey/update_questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Questions updated successfully."));

        // Verify that the controller delegated to the service.
        verify(surveyService).updateQuestions(any(UpdateQuestionsRequest.class));
    }

    @Test
    @WithMockUser(username = "testUser")
    public void testGetSurvey_Success() throws Exception {
        String sessionToken = "valid-session-token";
        // Prepare a mocked service response.
        Map<String, Object> serviceResponse = Map.of(
                "surveys", new Object[] {
                        Map.of("id", 1, "surveyName", "Survey A", "description", "Desc A"),
                        Map.of("id", 2, "surveyName", "Survey B", "description", "Desc B")
                }
        );
        when(surveyService.getAllSurveysForUser(sessionToken))
                .thenReturn(ResponseEntity.ok(serviceResponse));

        // Since the controller method accepts a raw String, we need to send a JSON string literal.
        String jsonSession = "{\"sessionToken\":\"" + sessionToken + "\"}";
        mockMvc.perform(post("/survey/getSurvey")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSession))
                .andExpect(status().isOk());

        // Verify that the controller delegated to the service.
        verify(surveyService).getAllSurveysForUser(sessionToken);
    }

    @Test
    @WithMockUser(username = "testUser")
    public void testGetSurveyDetail_Success() throws Exception {
        String sessionToken = "valid-session-token";
        // Prepare a mocked service response.
        Map<String, Object> serviceResponse = Map.of(
                "surveys", new Object[] {
                        Map.of("id", 1, "type", "single", "question_order", "1", "description", "Desc A", "body", "Body A"),
                }
        );
        when(surveyService.getSurveyDetail(any(GetSurveyDetailRequest.class)))
                .thenReturn(ResponseEntity.ok(serviceResponse));

        String jsonRequest = "{\"sessionToken\":\"valid-token\",\"surveyId\":\"1\"}";
        mockMvc.perform(post("/survey/get_survey_detail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk());

        // Verify that the controller delegated to the service.
        verify(surveyService).getSurveyDetail(any(GetSurveyDetailRequest.class));
    }

    @Test
    @WithMockUser(username = "testUser")
    public void testGetAccessibleSurveys() throws Exception {
        String sessionToken = "valid-session-token";
        // Prepare a mocked service response.
        Map<String, Object> serviceResponse = Map.of(
                "surveys", new Object[] {
                        Map.of("id", 1, "type", "single", "question_order", "1", "description", "Desc A", "body", "Body A"),
                }
        );
        when(surveyService.getSurveyDetail(any(GetSurveyDetailRequest.class)))
                .thenReturn(ResponseEntity.ok(serviceResponse));

        String jsonRequest = "{\"sessionToken\":\"valid-token\",\"surveyId\":\"1\"}";
        mockMvc.perform(post("/survey/get_accessible_survey")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk());
    }
}
