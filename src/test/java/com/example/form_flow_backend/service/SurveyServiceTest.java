package com.example.form_flow_backend.service;

import com.example.form_flow_backend.DTO.CreateSurveyRequest;
import com.example.form_flow_backend.model.Session;
import com.example.form_flow_backend.model.Survey;
import com.example.form_flow_backend.model.User;
import com.example.form_flow_backend.repository.QuestionRepository;
import com.example.form_flow_backend.repository.SessionRepository;
import com.example.form_flow_backend.repository.SurveyRepository;
import com.example.form_flow_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class SurveyServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private SurveyRepository surveyRepository;
    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private SessionRepository sessionRepository;
    @InjectMocks
    private SurveyService surveyService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void createSurvey_Success() {
        CreateSurveyRequest request = new CreateSurveyRequest();
        request.setSessionToken("valid-session-token");
        request.setSurveyName("My First Survey");
        request.setDescription("Just a test.");
        Session fakeSession = new Session();
        fakeSession.setSessionToken("valid-session-token");
        fakeSession.setUsername("testUser");
        when(sessionRepository.findBySessionToken("valid-session-token")).thenReturn(Optional.of(fakeSession));
        User fakeUser = new User();
        fakeUser.setId(1L);
        fakeUser.setUsername("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(fakeUser));
        Survey savedSurvey = new Survey();
        savedSurvey.setId(100L);
        savedSurvey.setSurveyName("My First Survey");
        when(surveyRepository.save(any(Survey.class))).thenReturn(savedSurvey);
        ResponseEntity<Map<String, Object>> response = surveyService.createSurvey(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals("Survey created successfully.", response.getBody().get("message"));
        assertEquals(100L, response.getBody().get("surveyId"));
        verify(sessionRepository, times(1)).findBySessionToken("valid-session-token");
        verify(userRepository, times(1)).findByUsername("testUser");
        verify(surveyRepository, times(1)).save(any(Survey.class));
        verifyNoMoreInteractions(sessionRepository, userRepository, surveyRepository);
    }

    @Test
    void createSurvey_InvalidSessionToken() {
        CreateSurveyRequest request = new CreateSurveyRequest();
        request.setSessionToken("invalid-token");
        request.setSurveyName("SurveyName");
        when(sessionRepository.findBySessionToken("invalid-token")).thenReturn(Optional.empty());
        ResponseEntity<Map<String, Object>> response = surveyService.createSurvey(request);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Session not found or invalid.", response.getBody().get("message"));
        verify(sessionRepository, times(1)).findBySessionToken("invalid-token");
        verifyNoMoreInteractions(sessionRepository, userRepository, surveyRepository);
    }

    @Test
    void createSurvey_UserNotFound() {
        CreateSurveyRequest request = new CreateSurveyRequest();
        request.setSessionToken("valid-session-token");
        request.setSurveyName("SurveyName");
        Session fakeSession = new Session();
        fakeSession.setSessionToken("valid-session-token");
        fakeSession.setUsername("nonExistentUser");
        when(sessionRepository.findBySessionToken("valid-session-token")).thenReturn(Optional.of(fakeSession));
        when(userRepository.findByUsername("nonExistentUser")).thenReturn(Optional.empty());
        ResponseEntity<Map<String, Object>> response = surveyService.createSurvey(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("User not found in database.", response.getBody().get("message"));
        verify(sessionRepository, times(1)).findBySessionToken("valid-session-token");
        verify(userRepository, times(1)).findByUsername("nonExistentUser");
        verifyNoMoreInteractions(sessionRepository, userRepository, surveyRepository);
    }

    @Test
    void createSurvey_SurveyNameMissing() {
        CreateSurveyRequest request = new CreateSurveyRequest();
        request.setSessionToken("valid-session-token");
        request.setDescription("Some description.");
        Session fakeSession = new Session();
        fakeSession.setSessionToken("valid-session-token");
        fakeSession.setUsername("testUser");
        when(sessionRepository.findBySessionToken("valid-session-token")).thenReturn(Optional.of(fakeSession));
        User fakeUser = new User();
        fakeUser.setId(1L);
        fakeUser.setUsername("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(fakeUser));
        ResponseEntity<Map<String, Object>> response = surveyService.createSurvey(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Survey name is required.", response.getBody().get("message"));
        verify(sessionRepository, times(1)).findBySessionToken("valid-session-token");
        verify(userRepository, times(1)).findByUsername("testUser");
        verifyNoMoreInteractions(sessionRepository, userRepository, surveyRepository);
    }

    @Test
    void getAllSurveysForUser_Success() {
        String validToken = "valid-session-token";
        Session fakeSession = new Session();
        fakeSession.setSessionToken(validToken);
        fakeSession.setUsername("testUser");
        when(sessionRepository.findBySessionToken(validToken)).thenReturn(Optional.of(fakeSession));
        User fakeUser = new User();
        fakeUser.setId(1L);
        fakeUser.setUsername("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(fakeUser));
        Survey s1 = new Survey();
        s1.setId(101L);
        s1.setSurveyName("Survey A");
        s1.setDescription("Desc A");
        Survey s2 = new Survey();
        s2.setId(102L);
        s2.setSurveyName("Survey B");
        s2.setDescription("Desc B");
        List<Survey> mockSurveys = Arrays.asList(s1, s2);
        when(surveyRepository.findAllByUser(fakeUser)).thenReturn(mockSurveys);
        ResponseEntity<Map<String, Object>> response = surveyService.getAllSurveysForUser(validToken);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, Object> body = response.getBody();
        assertTrue(body.containsKey("surveys"));
        List<Map<String, Object>> surveysResult = (List<Map<String, Object>>) body.get("surveys");
        assertEquals(2, surveysResult.size());
        Map<String, Object> survey1 = surveysResult.get(0);
        assertEquals(101L, survey1.get("id"));
        assertEquals("Survey A", survey1.get("surveyName"));
        assertEquals("Desc A", survey1.get("description"));
        Map<String, Object> survey2 = surveysResult.get(1);
        assertEquals(102L, survey2.get("id"));
        assertEquals("Survey B", survey2.get("surveyName"));
        assertEquals("Desc B", survey2.get("description"));
        verify(sessionRepository, times(1)).findBySessionToken(validToken);
        verify(userRepository, times(1)).findByUsername("testUser");
        verify(surveyRepository, times(1)).findAllByUser(fakeUser);
        verifyNoMoreInteractions(sessionRepository, userRepository, surveyRepository);
    }

    @Test
    void getAllSurveysForUser_SessionNotFound() {
        String invalidToken = "invalid-session-token";
        when(sessionRepository.findBySessionToken(invalidToken)).thenReturn(Optional.empty());
        ResponseEntity<Map<String, Object>> response = surveyService.getAllSurveysForUser(invalidToken);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Session not found or invalid.", response.getBody().get("message"));
        verify(sessionRepository, times(1)).findBySessionToken(invalidToken);
        verifyNoMoreInteractions(sessionRepository, userRepository, surveyRepository);
    }

    @Test
    void getAllSurveysForUser_UserNotFound() {
        String validToken = "valid-session-token";
        Session fakeSession = new Session();
        fakeSession.setSessionToken(validToken);
        fakeSession.setUsername("nonExistentUser");
        when(sessionRepository.findBySessionToken(validToken)).thenReturn(Optional.of(fakeSession));
        when(userRepository.findByUsername("nonExistentUser")).thenReturn(Optional.empty());
        ResponseEntity<Map<String, Object>> response = surveyService.getAllSurveysForUser(validToken);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("User not found in database.", response.getBody().get("message"));
        verify(sessionRepository, times(1)).findBySessionToken(validToken);
        verify(userRepository, times(1)).findByUsername("nonExistentUser");
        verifyNoMoreInteractions(sessionRepository, userRepository, surveyRepository);
    }
}
