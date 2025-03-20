package com.example.form_flow_backend.service;

import com.example.form_flow_backend.DTO.TakeSurveyRequest;
import com.example.form_flow_backend.model.*;
import com.example.form_flow_backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TakeServiceTest {

    @Mock
    private TakesRepository takesRepository;
    @Mock
    private SessionRepository sessionRepository;
    @Mock
    private SessionService sessionService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SurveyRepository surveyRepository;

    @InjectMocks
    private TakeService takeService;

    private TakeSurveyRequest request;

    @BeforeEach
    void setUp() {
        request = new TakeSurveyRequest();
    }

    @Test
    void testTakeSurvey_sessionTokenNull() {
        request.setSessionToken(null);
        ResponseEntity<Map<String, Object>> response = takeService.takeSurvey(request);

        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().get("success").equals(true));
        assertEquals("Session token is missing.", response.getBody().get("message"));
    }

    @Test
    void testTakeSurvey_sessionInvalidOrExpired() {
        request.setSessionToken("invalidToken");

        when(sessionService.verifySession("invalidToken")).thenReturn(false);

        ResponseEntity<Map<String, Object>> response = takeService.takeSurvey(request);

        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().get("success").equals(true));
        assertEquals("Unauthorized or session expired.", response.getBody().get("message"));
    }

    @Test
    void testTakeSurvey_userNotFound() {
        request.setSessionToken("validToken");
        when(sessionService.verifySession("validToken")).thenReturn(true);

        Session mockSession = new Session();
        mockSession.setUsername("missingUser");
        when(sessionRepository.findBySessionToken("validToken")).thenReturn(Optional.of(mockSession));
        when(userRepository.findByUsername("missingUser")).thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> response = takeService.takeSurvey(request);

        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().get("success").equals(true));
        assertEquals("User not found in database.", response.getBody().get("message"));
    }

    @Test
    void testTakeSurvey_invalidSurveyId() {
        request.setSessionToken("validToken");
        request.setSurveyId("notANumber");
        when(sessionService.verifySession("validToken")).thenReturn(true);

        Session mockSession = new Session();
        mockSession.setUsername("someUser");
        when(sessionRepository.findBySessionToken("validToken")).thenReturn(Optional.of(mockSession));
        when(userRepository.findByUsername("someUser")).thenReturn(Optional.of(new User()));

        ResponseEntity<Map<String, Object>> response = takeService.takeSurvey(request);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Invalid survey ID.", response.getBody().get("message"));
    }

    @Test
    void testTakeSurvey_surveyNotFound() {
        request.setSessionToken("validToken");
        request.setSurveyId("123"); // valid long
        when(sessionService.verifySession("validToken")).thenReturn(true);

        Session mockSession = new Session();
        mockSession.setUsername("someUser");
        when(sessionRepository.findBySessionToken("validToken")).thenReturn(Optional.of(mockSession));

        User user = new User();
        user.setId(100L);
        when(userRepository.findByUsername("someUser")).thenReturn(Optional.of(user));

        when(surveyRepository.findById(123L)).thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> response = takeService.takeSurvey(request);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Survey not found in database.", response.getBody().get("message"));
    }

    @Test
    void testTakeSurvey_success() {
        request.setSessionToken("validToken");
        request.setSurveyId("123");
        request.setAnswers("answer_data");

        when(sessionService.verifySession("validToken")).thenReturn(true);

        Session mockSession = new Session();
        mockSession.setUsername("someUser");
        when(sessionRepository.findBySessionToken("validToken")).thenReturn(Optional.of(mockSession));

        User user = new User();
        user.setId(100L);
        when(userRepository.findByUsername("someUser")).thenReturn(Optional.of(user));

        Survey survey = new Survey();
        survey.setId(123L);
        when(surveyRepository.findById(123L)).thenReturn(Optional.of(survey));

        ResponseEntity<Map<String, Object>> response = takeService.takeSurvey(request);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals("Answers saved successfully.", response.getBody().get("message"));

        // 验证数据是否被保存
        verify(takesRepository, times(1)).save(any(Takes.class));
    }
}
