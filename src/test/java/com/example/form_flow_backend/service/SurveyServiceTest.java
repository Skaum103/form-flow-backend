package com.example.form_flow_backend.service;

import com.example.form_flow_backend.DTO.CreateSurveyRequest;
import com.example.form_flow_backend.DTO.GetSurveyDetailRequest;
import com.example.form_flow_backend.DTO.UpdateQuestionsRequest;
import com.example.form_flow_backend.model.Question;
import com.example.form_flow_backend.model.Session;
import com.example.form_flow_backend.model.Survey;
import com.example.form_flow_backend.model.User;
import com.example.form_flow_backend.repository.Access.AccessRepository;
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
    @Mock
    private SessionService sessionService;
    @Mock
    private AccessRepository accessRepository;

    @BeforeEach
    void setUp() {
    }

    @Test
    void createSurvey_MissingSessionToken() {
        CreateSurveyRequest request = new CreateSurveyRequest();
        request.setSurveyName("My First Survey");
        request.setDescription("Just a test.");
        ResponseEntity<Map<String, Object>> response = surveyService.createSurvey(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("success"));
        assertEquals("Session token is missing.", body.get("message"));
    }

    @Test
    void createSurvey_Success() {
        CreateSurveyRequest request = new CreateSurveyRequest();
        request.setSessionToken("valid-session-token");
        request.setSurveyName("My First Survey");
        request.setDescription("Just a test.");
        request.setAccessControl("-1");
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
        when(accessRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
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
        when(sessionService.verifySession("invalid-token")).thenReturn(false);
        ResponseEntity<Map<String, Object>> response = surveyService.createSurvey(request);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Unauthorized or session expired.", response.getBody().get("message"));
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
    void getAllSurveysForUser_MissingSessionToken() {
        ResponseEntity<Map<String, Object>> response = surveyService.getAllSurveysForUser(null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("success"));
        assertEquals("Session token is missing.", body.get("message"));
    }

    @Test
    void getAllSurveysForUser_Success() {
        String validToken = "valid-token";
        Session fakeSession = new Session();
        fakeSession.setSessionToken(validToken);
        fakeSession.setUsername("testUser");
        when(sessionService.verifySession("valid-token")).thenReturn(true);
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
        assertEquals(101L, survey1.get("surveyId"));
        assertEquals("Survey A", survey1.get("surveyName"));
        assertEquals("Desc A", survey1.get("description"));

        Map<String, Object> survey2 = surveysResult.get(1);
        assertEquals(102L, survey2.get("surveyId"));
        assertEquals("Survey B", survey2.get("surveyName"));
        assertEquals("Desc B", survey2.get("description"));
        verify(sessionRepository, times(1)).findBySessionToken(validToken);
        verify(userRepository, times(1)).findByUsername("testUser");
        verify(surveyRepository, times(1)).findAllByUser(fakeUser);
        verifyNoMoreInteractions(sessionRepository, userRepository, surveyRepository);
    }

    @Test
    void getAllSurveysForUser_SessionNotFound() {
        String invalidToken = "invalid-token";
        when(sessionService.verifySession("invalid-token")).thenReturn(false);

        ResponseEntity<Map<String, Object>> response = surveyService.getAllSurveysForUser(invalidToken);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Unauthorized or session expired.", response.getBody().get("message"));

        verify(sessionService, times(1)).verifySession(invalidToken);
        verifyNoMoreInteractions(sessionRepository, userRepository, surveyRepository);
    }

    @Test
    void getAllSurveysForUser_UserNotFound() {
        String validToken = "valid-token";
        Session fakeSession = new Session();
        fakeSession.setSessionToken(validToken);
        fakeSession.setUsername("nonExistentUser");

        when(sessionService.verifySession("valid-token")).thenReturn(true);
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

    @Test
    void updateQuestions_MissingSessionToken() {
        UpdateQuestionsRequest request = new UpdateQuestionsRequest();
        request.setSessionToken(null);
        request.setSurveyId("123");
        // Questions can be null for this test

        ResponseEntity<Map<String, Object>> response = surveyService.updateQuestions(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("success"));
        assertEquals("Session token is missing.", body.get("message"));
    }

    @Test
    void updateQuestions_InvalidSessionToken() {
        UpdateQuestionsRequest request = new UpdateQuestionsRequest();
        request.setSessionToken("invalid-token");
        request.setSurveyId("123");

        // Simulate that session lookup fails.
        when(sessionService.verifySession("invalid-token")).thenReturn(false);

        ResponseEntity<Map<String, Object>> response = surveyService.updateQuestions(request);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("success"));
        assertEquals("Unauthorized or session expired.", body.get("message"));
    }

    @Test
    void updateQuestions_InvalidSurveyIdFormat() {
        UpdateQuestionsRequest request = new UpdateQuestionsRequest();
        request.setSessionToken("valid-token");
        request.setSurveyId("abc");

        // Simulate a valid session.
        when(sessionService.verifySession("valid-token")).thenReturn(true);

        ResponseEntity<Map<String, Object>> response = surveyService.updateQuestions(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("success"));
        assertEquals("Invalid survey ID.", body.get("message"));
    }

    @Test
    void updateQuestions_SurveyNotFound() {
        UpdateQuestionsRequest request = new UpdateQuestionsRequest();
        request.setSessionToken("valid-token");
        request.setSurveyId("123");

        // Simulate a valid session.
        when(sessionService.verifySession("valid-token")).thenReturn(true);

        // Survey not found.
        when(surveyRepository.findById(123L)).thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> response = surveyService.updateQuestions(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("success"));
        assertEquals("Survey not found in database.", body.get("message"));
    }

    @Test
    void updateQuestions_SuccessWithQuestions() {
        UpdateQuestionsRequest request = new UpdateQuestionsRequest();
        request.setSessionToken("valid-token");
        request.setSurveyId("123");

        // Prepare a list of questions.
        List<Question> questions = new ArrayList<>();
        Question q1 = new Question();
        q1.setDescription("q1");
        q1.setBody("q1body");
        questions.add(q1);
        Question q2 = new Question();
        q2.setDescription("q2");
        q2.setBody("q2body");
        questions.add(q2);
        request.setQuestions(questions);

        // Simulate valid session.
        when(sessionService.verifySession("valid-token")).thenReturn(true);

        // Simulate survey found.
        Survey survey = new Survey();
        survey.setId(123L);
        when(surveyRepository.findById(123L)).thenReturn(Optional.of(survey));

        // Simulate deletion and saving.
        doNothing().when(questionRepository).deleteBySurveyId(123L);
        when(questionRepository.saveAll(questions)).thenReturn(questions);

        ResponseEntity<Map<String, Object>> response = surveyService.updateQuestions(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(true, body.get("success"));
        assertEquals("Questions updated successfully.", body.get("message"));

        // Verify each question has been assigned the survey.
        for (Question q : questions) {
            assertEquals(survey, q.getSurvey());
        }
        verify(questionRepository).deleteBySurveyId(123L);
        verify(questionRepository).saveAll(questions);
    }

    @Test
    void updateQuestions_SuccessNoQuestions() {
        UpdateQuestionsRequest request = new UpdateQuestionsRequest();
        request.setSessionToken("valid-token");
        request.setSurveyId("123");
        request.setQuestions(null);

        // Simulate valid session.
        when(sessionService.verifySession("valid-token")).thenReturn(true);

        // Simulate survey found.
        Survey survey = new Survey();
        survey.setId(123L);
        when(surveyRepository.findById(123L)).thenReturn(Optional.of(survey));

        doNothing().when(questionRepository).deleteBySurveyId(123L);

        ResponseEntity<Map<String, Object>> response = surveyService.updateQuestions(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(true, body.get("success"));
        assertEquals("Questions updated successfully.", body.get("message"));

        verify(questionRepository).deleteBySurveyId(123L);
        verify(questionRepository, never()).saveAll(anyList());
    }

    @Test
    void getSurveyDetails_MissingSessionToken() {
        GetSurveyDetailRequest request = new GetSurveyDetailRequest();
        request.setSessionToken(null);
        request.setSurveyId("123");

        ResponseEntity<Map<String, Object>> response = surveyService.getSurveyDetail(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("success"));
        assertEquals("Session token is missing.", body.get("message"));
    }

    @Test
    void getSurveyDetails_InvalidSessionToken() {
        GetSurveyDetailRequest request = new GetSurveyDetailRequest();
        request.setSessionToken("invalid-token");

        // Simulate that session lookup fails.
        when(sessionService.verifySession("invalid-token")).thenReturn(false);

        ResponseEntity<Map<String, Object>> response = surveyService.getSurveyDetail(request);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("success"));
        assertEquals("Unauthorized or session expired.", body.get("message"));
    }

    @Test
    void getSurveyDetails_InvalidSurveyIdFormat() {
        GetSurveyDetailRequest request = new GetSurveyDetailRequest();
        request.setSessionToken("valid-token");
        request.setSurveyId("abc");

        // Simulate a valid session.
        when(sessionService.verifySession("valid-token")).thenReturn(true);

        ResponseEntity<Map<String, Object>> response = surveyService.getSurveyDetail(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("success"));
        assertEquals("Invalid survey ID.", body.get("message"));
    }

    @Test
    void getSurveyDetails_SurveyNotFound() {
        GetSurveyDetailRequest request = new GetSurveyDetailRequest();
        request.setSessionToken("valid-token");
        request.setSurveyId("123");

        // Simulate a valid session.
        when(sessionService.verifySession("valid-token")).thenReturn(true);
        // Survey not found.
        when(surveyRepository.findById(123L)).thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> response = surveyService.getSurveyDetail(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("success"));
        assertEquals("Survey not found in database.", body.get("message"));
    }

    @Test
    void getSurveyDetails_Success() {
        GetSurveyDetailRequest request = new GetSurveyDetailRequest();
        request.setSessionToken("valid-token");
        request.setSurveyId("123");

        // Simulate a valid session.
        when(sessionService.verifySession("valid-token")).thenReturn(true);

        // Simulate survey found.
        Survey survey = new Survey();
        when(surveyRepository.findById(123L)).thenReturn(Optional.of(survey));
        Question q = new Question();
        q.setId(1L);
        List<Question> questions = Collections.singletonList(q);
        when(questionRepository.findBySurveyId(123L)).thenReturn(Optional.of(questions));

        ResponseEntity<Map<String, Object>> response = surveyService.getSurveyDetail(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
    }
}
