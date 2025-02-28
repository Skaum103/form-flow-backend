package com.example.form_flow_backend.controller;

import com.example.form_flow_backend.DTO.CreateSurveyRequest;
import com.example.form_flow_backend.service.SessionService;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SurveyControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private SurveyService surveyService;
    @MockBean
    private SessionService sessionService;

    @Test
    @WithMockUser(username = "loggedInUser")
    void createSurvey_Success() throws Exception {
        Map<String, Object> serviceResponse = new HashMap<>();
        serviceResponse.put("success", true);
        serviceResponse.put("message", "Survey created successfully");
        serviceResponse.put("surveyId", 123L);
        when(surveyService.createSurvey(any(CreateSurveyRequest.class))).thenReturn(ResponseEntity.ok(serviceResponse));
        mockMvc.perform(post("/survey/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionToken\":\"test-token\",\"surveyName\":\"Test Survey\",\"description\":\"Just a test\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Survey created successfully"))
                .andExpect(jsonPath("$.surveyId").value(123L));
        verify(surveyService, times(1)).createSurvey(any(CreateSurveyRequest.class));
    }

    @Test
    @WithMockUser(username = "loggedInUser")
    void createSurvey_MissingSurveyName() throws Exception {
        Map<String, Object> serviceResponse = new HashMap<>();
        serviceResponse.put("success", false);
        serviceResponse.put("message", "Survey name is required.");
        when(surveyService.createSurvey(any(CreateSurveyRequest.class))).thenReturn(ResponseEntity.badRequest().body(serviceResponse));
        mockMvc.perform(post("/survey/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionToken\":\"test-token\",\"description\":\"Just a test\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Survey name is required."));
        verify(surveyService, times(1)).createSurvey(any(CreateSurveyRequest.class));
    }

    @Test
    @WithMockUser(username = "loggedInUser")
    void getSurvey_Success() throws Exception {
        when(sessionService.verifySession("valid-session-token")).thenReturn(true);
        Map<String, Object> mockResponseBody = new HashMap<>();
        List<Map<String, Object>> mockSurveys = new ArrayList<>();
        Map<String, Object> s1 = new HashMap<>();
        s1.put("id", 1);
        s1.put("surveyName", "Survey A");
        s1.put("description", "Desc A");
        Map<String, Object> s2 = new HashMap<>();
        s2.put("id", 2);
        s2.put("surveyName", "Survey B");
        s2.put("description", "Desc B");
        mockSurveys.add(s1);
        mockSurveys.add(s2);
        mockResponseBody.put("surveys", mockSurveys);
        when(surveyService.getAllSurveysForUser("valid-session-token")).thenReturn(ResponseEntity.ok(mockResponseBody));
        mockMvc.perform(post("/survey/getSurvey")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionToken\":\"valid-session-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.surveys").exists())
                .andExpect(jsonPath("$.surveys[0].id").value(1))
                .andExpect(jsonPath("$.surveys[0].surveyName").value("Survey A"))
                .andExpect(jsonPath("$.surveys[1].id").value(2))
                .andExpect(jsonPath("$.surveys[1].surveyName").value("Survey B"));
        verify(surveyService, times(1)).getAllSurveysForUser("valid-session-token");
    }

    @Test
    @WithMockUser(username = "loggedInUser")
    void getSurvey_MissingSessionToken() throws Exception {
        mockMvc.perform(post("/survey/getSurvey")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Session token is missing."));
        verify(surveyService, never()).getAllSurveysForUser(any());
    }

    @Test
    @WithMockUser(username = "loggedInUser")
    void getSurvey_UnauthorizedSessionToken() throws Exception {
        when(sessionService.verifySession("invalid-token")).thenReturn(false);
        when(surveyService.getAllSurveysForUser("invalid-token")).thenReturn(ResponseEntity.status(401).body(Map.of("error","invalid")));
        mockMvc.perform(post("/survey/getSurvey")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionToken\":\"invalid-token\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Unauthorized or session expired."));
        verify(surveyService, never()).getAllSurveysForUser("invalid-token");
    }
}
