package com.example.form_flow_backend.service;

import com.example.form_flow_backend.DTO.GetSurveyDetailRequest;
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


    @Test
    void testGetSurveyTakeStatistics_sessionTokenNull() {
        // 构造请求, 不传 sessionToken
        GetSurveyDetailRequest req = new GetSurveyDetailRequest();
        req.setSurveyId("123");

        ResponseEntity<Map<String, Object>> response = takeService.getSurveyTakeStatistics(req);

        assertEquals(400, response.getStatusCodeValue());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Session token is missing.", response.getBody().get("message"));
    }

    @Test
    void testGetSurveyTakeStatistics_sessionInvalid() {
        GetSurveyDetailRequest req = new GetSurveyDetailRequest();
        req.setSessionToken("invalid");
        req.setSurveyId("123");

        when(sessionService.verifySession("invalid")).thenReturn(false);

        ResponseEntity<Map<String, Object>> response = takeService.getSurveyTakeStatistics(req);

        assertEquals(401, response.getStatusCodeValue());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Unauthorized or session expired.", response.getBody().get("message"));
    }

    @Test
    void testGetSurveyTakeStatistics_invalidSurveyId() {
        GetSurveyDetailRequest req = new GetSurveyDetailRequest();
        req.setSessionToken("validToken");
        req.setSurveyId("abc"); // 非数字

        when(sessionService.verifySession("validToken")).thenReturn(true);

        // sessionRepository.findBySessionToken(...) 省略,若需要也可以Mock
        ResponseEntity<Map<String, Object>> response = takeService.getSurveyTakeStatistics(req);

        assertEquals(401, response.getStatusCodeValue());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Unauthorized or session expired. (Session not found in DB)", response.getBody().get("message"));
    }

    @Test
    void testGetSurveyTakeStatistics_noTakesFound() {
        GetSurveyDetailRequest req = new GetSurveyDetailRequest();
        req.setSessionToken("validToken");
        req.setSurveyId("999");

        when(sessionService.verifySession("validToken")).thenReturn(true);

        Session mockSession = new Session();
        mockSession.setUsername("testUser");
        when(sessionRepository.findBySessionToken("validToken"))
                .thenReturn(Optional.of(mockSession));

        // findTakesBySurveyId 返回空 => no takes found
        when(takesRepository.findTakesBySurveyId(999L)).thenReturn(Collections.emptyList());

        ResponseEntity<Map<String, Object>> response = takeService.getSurveyTakeStatistics(req);
        assertEquals(400, response.getStatusCodeValue());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("No takes found for this survey.", response.getBody().get("message"));
    }

    @Test
    void testGetSurveyTakeStatistics_success_singleChoice() {
        GetSurveyDetailRequest req = new GetSurveyDetailRequest();
        req.setSessionToken("validToken");
        req.setSurveyId("888");

        when(sessionService.verifySession("validToken")).thenReturn(true);

        Session mockSession = new Session();
        mockSession.setUsername("testUser");
        when(sessionRepository.findBySessionToken("validToken"))
                .thenReturn(Optional.of(mockSession));

        // 构造 Takes: "A;B" => 两题分别回答 "A", "B"
        Takes t1 = new Takes();
        t1.setAnswers("A;B");

        Takes t2 = new Takes();
        t2.setAnswers("A;C"); // 第2题回答"C"

        List<Takes> allTakes = Arrays.asList(t1, t2);
        when(takesRepository.findTakesBySurveyId(888L)).thenReturn(allTakes);

        ResponseEntity<Map<String, Object>> response = takeService.getSurveyTakeStatistics(req);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue((Boolean) response.getBody().get("success"));

        // 拿到 stats
        List<?> stats = (List<?>) response.getBody().get("stats");
        assertEquals(2, stats.size()); // 2个question
        // 你可继续断言 stats[0] 中A=2; stats[1] 中 B=1, C=1 等
    }

    @Test
    void testGetSurveyTakeStatistics_success_multiChoice() {
        // 测试第1题回答 "A,B" 多选
        GetSurveyDetailRequest req = new GetSurveyDetailRequest();
        req.setSessionToken("validToken");
        req.setSurveyId("777");

        when(sessionService.verifySession("validToken")).thenReturn(true);

        Session mockSession = new Session();
        mockSession.setUsername("testUser");
        when(sessionRepository.findBySessionToken("validToken"))
                .thenReturn(Optional.of(mockSession));

        Takes t1 = new Takes();
        // 第1题: "A,B"; 第2题: "X"
        t1.setAnswers("A,B;X");

        List<Takes> allTakes = Collections.singletonList(t1);
        when(takesRepository.findTakesBySurveyId(777L)).thenReturn(allTakes);

        ResponseEntity<Map<String, Object>> response = takeService.getSurveyTakeStatistics(req);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue((Boolean) response.getBody().get("success"));

        // 断言 stats
        List<?> stats = (List<?>) response.getBody().get("stats");
        assertEquals(2, stats.size());
        // ...
    }

}
