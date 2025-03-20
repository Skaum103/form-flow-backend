package com.example.form_flow_backend.service;

import com.example.form_flow_backend.DTO.CreateSurveyRequest;
import com.example.form_flow_backend.DTO.GetSurveyDetailRequest;
import com.example.form_flow_backend.DTO.UpdateQuestionsRequest;
import com.example.form_flow_backend.model.*;
import com.example.form_flow_backend.repository.*;
import com.example.form_flow_backend.repository.Access.AccessRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SurveyServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private SurveyRepository surveyRepository;
    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private SessionRepository sessionRepository;
    @Mock
    private SessionService sessionService;
    @Mock
    private AccessRepository accessRepository;

    @InjectMocks
    private SurveyService surveyService;

    private Session mockSession;
    private User mockUser;
    private Survey mockSurvey;

    @BeforeEach
    void setUp() {
        // 基础模拟数据
        mockSession = new Session();
        mockSession.setUsername("testUser");

        mockUser = new User();
        mockUser.setUsername("testUser");
        mockUser.setEmail("testUser@example.com");
        mockUser.setPassword("pass123");

        mockSurvey = new Survey();
        mockSurvey.setId(1L);
        mockSurvey.setSurveyName("MySurvey");
        mockSurvey.setDescription("Desc");
        mockSurvey.setUser(mockUser);
    }

    // ---------------------------
    // 1) createSurvey 测试
    // ---------------------------
    @Test
    void createSurvey_MissingSessionToken() {
        CreateSurveyRequest request = new CreateSurveyRequest();
        request.setSurveyName("My First Survey");
        request.setDescription("Just a test.");
        request.setAccessControl("-1");

        // 未设置 sessionToken
        ResponseEntity<Map<String, Object>> response = surveyService.createSurvey(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("Session token is missing.", response.getBody().get("message"));
    }

    @Test
    void createSurvey_SessionNotFound() {
        CreateSurveyRequest request = new CreateSurveyRequest();
        request.setSessionToken("invalidToken");
        request.setSurveyName("SurveyName");
        request.setDescription("desc");
        request.setAccessControl("-1");

        when(sessionRepository.findBySessionToken("invalidToken")).thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> response = surveyService.createSurvey(request);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("Unauthorized or session expired.", response.getBody().get("message"));
    }

    @Test
    void createSurvey_UserNotFound() {
        CreateSurveyRequest request = new CreateSurveyRequest();
        request.setSessionToken("validToken");
        request.setSurveyName("SurveyName");
        request.setDescription("desc");
        request.setAccessControl("-1");

        when(sessionRepository.findBySessionToken("validToken")).thenReturn(Optional.of(mockSession));
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> response = surveyService.createSurvey(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("User not found in database.", response.getBody().get("message"));
    }

    @Test
    void createSurvey_MissingSurveyName() {
        CreateSurveyRequest request = new CreateSurveyRequest();
        request.setSessionToken("validToken");
        request.setSurveyName(" "); // 空格
        request.setDescription("desc");
        request.setAccessControl("-1");

        when(sessionRepository.findBySessionToken("validToken")).thenReturn(Optional.of(mockSession));
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(mockUser));

        ResponseEntity<Map<String, Object>> response = surveyService.createSurvey(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("Survey name is required.", response.getBody().get("message"));
    }

    @Test
    void createSurvey_Success_WithUniversalAccess() {
        CreateSurveyRequest request = new CreateSurveyRequest();
        request.setSessionToken("validToken");
        request.setSurveyName("MySurvey");
        request.setDescription("desc");
        // 测试 -1 访问场景
        request.setAccessControl("-1");

        when(sessionRepository.findBySessionToken("validToken")).thenReturn(Optional.of(mockSession));
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(mockUser));
        when(surveyRepository.save(any(Survey.class))).thenAnswer(inv -> {
            Survey s = inv.getArgument(0);
            s.setId(100L);
            return s;
        });

        ResponseEntity<Map<String, Object>> response = surveyService.createSurvey(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody().get("success"));
        assertEquals("Survey created successfully.", response.getBody().get("message"));
        assertEquals(100L, response.getBody().get("surveyId"));

        // 确认saveAll被调用
        verify(accessRepository, times(1)).saveAll(anyList());
    }

    @Test
    void createSurvey_Success_WithNamedAccess() {
        CreateSurveyRequest request = new CreateSurveyRequest();
        request.setSessionToken("validToken");
        request.setSurveyName("MySurvey");
        request.setDescription("desc");
        // 测试普通用户访问
        request.setAccessControl("alice,bob");

        User alice = new User();
        alice.setUsername("alice");
        alice.setEmail("alice@example.com");
        alice.setPassword("passA");

        User bob = new User();
        bob.setUsername("bob");
        bob.setEmail("bob@example.com");
        bob.setPassword("passB");

        when(sessionRepository.findBySessionToken("validToken")).thenReturn(Optional.of(mockSession));
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(mockUser));
        when(surveyRepository.save(any(Survey.class))).thenAnswer(inv -> {
            Survey s = inv.getArgument(0);
            s.setId(101L);
            return s;
        });
        when(userRepository.findByUsernameIn(anyList())).thenReturn(Arrays.asList(alice, bob));

        ResponseEntity<Map<String, Object>> response = surveyService.createSurvey(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody().get("success"));
        assertEquals("Survey created successfully.", response.getBody().get("message"));
        assertEquals(101L, response.getBody().get("surveyId"));

        verify(accessRepository, times(1)).saveAll(anyList());
    }

    // ---------------------------
    // 2) getAllSurveysForUser 测试
    // ---------------------------
    @Test
    void getAllSurveysForUser_MissingSessionToken() {
        ResponseEntity<Map<String, Object>> response = surveyService.getAllSurveysForUser(null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("Session token is missing.", response.getBody().get("message"));
    }

    @Test
    void getAllSurveysForUser_InvalidSession() {
        when(sessionService.verifySession("invalidToken")).thenReturn(false);
        ResponseEntity<Map<String, Object>> response =
                surveyService.getAllSurveysForUser("invalidToken");
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("Unauthorized or session expired.", response.getBody().get("message"));
    }

    @Test
    void getAllSurveysForUser_UserNotFound() {
        when(sessionService.verifySession("validToken")).thenReturn(true);
        Session session = new Session();
        session.setUsername("unknownUser");
        when(sessionRepository.findBySessionToken("validToken"))
                .thenReturn(Optional.of(session));
        when(userRepository.findByUsername("unknownUser"))
                .thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> response =
                surveyService.getAllSurveysForUser("validToken");
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("User not found in database.", response.getBody().get("message"));
    }

    @Test
    void getAllSurveysForUser_Success() {
        when(sessionService.verifySession("validToken")).thenReturn(true);
        when(sessionRepository.findBySessionToken("validToken"))
                .thenReturn(Optional.of(mockSession));
        when(userRepository.findByUsername("testUser"))
                .thenReturn(Optional.of(mockUser));

        List<Survey> mockSurveyList = new ArrayList<>();
        Survey s1 = new Survey();
        s1.setId(11L);
        s1.setSurveyName("Survey11");
        s1.setUser(mockUser);
        Survey s2 = new Survey();
        s2.setId(12L);
        s2.setSurveyName("Survey12");
        s2.setUser(mockUser);
        mockSurveyList.add(s1);
        mockSurveyList.add(s2);

        when(surveyRepository.findAllByUser(mockUser)).thenReturn(mockSurveyList);

        ResponseEntity<Map<String, Object>> response =
                surveyService.getAllSurveysForUser("validToken");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().get("surveys"));

        List<?> surveys = (List<?>) response.getBody().get("surveys");
        assertEquals(2, surveys.size());
    }

    // ---------------------------
    // 3) updateQuestions 测试
    // ---------------------------
    @Test
    void updateQuestions_MissingSessionToken() {
        UpdateQuestionsRequest req = new UpdateQuestionsRequest();
        req.setSessionToken(null);

        ResponseEntity<Map<String, Object>> response = surveyService.updateQuestions(req);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("Session token is missing.", response.getBody().get("message"));
    }

    @Test
    void updateQuestions_InvalidSession() {
        UpdateQuestionsRequest req = new UpdateQuestionsRequest();
        req.setSessionToken("tokenX");
        req.setSurveyId("1");

        when(sessionService.verifySession("tokenX")).thenReturn(false);

        ResponseEntity<Map<String, Object>> response = surveyService.updateQuestions(req);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("Unauthorized or session expired.", response.getBody().get("message"));
    }

    @Test
    void updateQuestions_InvalidSurveyId() {
        UpdateQuestionsRequest req = new UpdateQuestionsRequest();
        req.setSessionToken("validToken");
        req.setSurveyId("abc"); // 非数字

        when(sessionService.verifySession("validToken")).thenReturn(true);

        ResponseEntity<Map<String, Object>> response = surveyService.updateQuestions(req);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("Invalid survey ID.", response.getBody().get("message"));
    }

    @Test
    void updateQuestions_SurveyNotFound() {
        UpdateQuestionsRequest req = new UpdateQuestionsRequest();
        req.setSessionToken("validToken");
        req.setSurveyId("99");

        when(sessionService.verifySession("validToken")).thenReturn(true);
        when(surveyRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> response = surveyService.updateQuestions(req);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("Survey not found in database.", response.getBody().get("message"));
    }

    @Test
    void updateQuestions_Success_NoQuestions() {
        UpdateQuestionsRequest req = new UpdateQuestionsRequest();
        req.setSessionToken("validToken");
        req.setSurveyId("1");
        // questions=null

        when(sessionService.verifySession("validToken")).thenReturn(true);
        when(surveyRepository.findById(1L)).thenReturn(Optional.of(mockSurvey));

        ResponseEntity<Map<String, Object>> response = surveyService.updateQuestions(req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody().get("success"));
        assertEquals("Questions updated successfully.", response.getBody().get("message"));
        verify(questionRepository, times(1)).deleteBySurveyId(1L);
        verify(questionRepository, never()).saveAll(anyList());
    }

    @Test
    void updateQuestions_Success_WithQuestions() {
        UpdateQuestionsRequest req = new UpdateQuestionsRequest();
        req.setSessionToken("validToken");
        req.setSurveyId("1");

        Question q1 = new Question();
        q1.setBody("Q1 body");
        Question q2 = new Question();
        q2.setBody("Q2 body");
        List<Question> questionList = Arrays.asList(q1, q2);
        req.setQuestions(questionList);

        when(sessionService.verifySession("validToken")).thenReturn(true);
        when(surveyRepository.findById(1L)).thenReturn(Optional.of(mockSurvey));

        ResponseEntity<Map<String, Object>> response = surveyService.updateQuestions(req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody().get("success"));
        assertEquals("Questions updated successfully.", response.getBody().get("message"));

        verify(questionRepository, times(1)).deleteBySurveyId(1L);
        verify(questionRepository, times(1)).saveAll(questionList);
    }

    // ---------------------------
    // 4) getSurveyDetail 测试
    // ---------------------------
    @Test
    void getSurveyDetail_MissingSessionToken() {
        GetSurveyDetailRequest req = new GetSurveyDetailRequest();
        req.setSessionToken(null);

        ResponseEntity<Map<String, Object>> response = surveyService.getSurveyDetail(req);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("Session token is missing.", response.getBody().get("message"));
    }

    @Test
    void getSurveyDetail_UnauthorizedSession() {
        GetSurveyDetailRequest req = new GetSurveyDetailRequest();
        req.setSessionToken("tokenX");
        req.setSurveyId("1");

        when(sessionService.verifySession("tokenX")).thenReturn(false);

        ResponseEntity<Map<String, Object>> response = surveyService.getSurveyDetail(req);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("Unauthorized or session expired.", response.getBody().get("message"));
    }

    @Test
    void getSurveyDetail_InvalidSurveyId() {
        GetSurveyDetailRequest req = new GetSurveyDetailRequest();
        req.setSessionToken("validToken");
        req.setSurveyId("abc");

        when(sessionService.verifySession("validToken")).thenReturn(true);

        ResponseEntity<Map<String, Object>> response = surveyService.getSurveyDetail(req);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("Invalid survey ID.", response.getBody().get("message"));
    }

    @Test
    void getSurveyDetail_SurveyNotFound() {
        GetSurveyDetailRequest req = new GetSurveyDetailRequest();
        req.setSessionToken("validToken");
        req.setSurveyId("99");

        when(sessionService.verifySession("validToken")).thenReturn(true);
        when(surveyRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> response = surveyService.getSurveyDetail(req);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("Survey not found in database.", response.getBody().get("message"));
    }

    @Test
    void getSurveyDetail_Success_NoQuestions() {
        GetSurveyDetailRequest req = new GetSurveyDetailRequest();
        req.setSessionToken("validToken");
        req.setSurveyId("1");

        when(sessionService.verifySession("validToken")).thenReturn(true);
        when(surveyRepository.findById(1L)).thenReturn(Optional.of(mockSurvey));
        when(questionRepository.findBySurveyId(1L)).thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> response = surveyService.getSurveyDetail(req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().get("questions"));
    }

    @Test
    void getSurveyDetail_Success_WithQuestions() {
        GetSurveyDetailRequest req = new GetSurveyDetailRequest();
        req.setSessionToken("validToken");
        req.setSurveyId("1");

        Question q1 = new Question();
        q1.setId(101L);
        q1.setBody("Q1");
        q1.setSurvey(mockSurvey);

        Question q2 = new Question();
        q2.setId(102L);
        q2.setBody("Q2");
        q2.setSurvey(mockSurvey);

        when(sessionService.verifySession("validToken")).thenReturn(true);
        when(surveyRepository.findById(1L)).thenReturn(Optional.of(mockSurvey));
        when(questionRepository.findBySurveyId(1L))
                .thenReturn(Optional.of(Arrays.asList(q1, q2)));

        ResponseEntity<Map<String, Object>> response = surveyService.getSurveyDetail(req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().get("questions"));

        // 确认question里的survey被置空
        Optional<List<Question>> questionListOpt =
                (Optional<List<Question>>) response.getBody().get("questions");
        assertTrue(questionListOpt.isPresent());
        assertNull(questionListOpt.get().get(0).getSurvey());
        assertNull(questionListOpt.get().get(1).getSurvey());
    }

    // ---------------------------
    // 5) getAccessibleSurvey 测试
    // ---------------------------
    @Test
    void getAccessibleSurvey_MissingSessionToken() {
        ResponseEntity<Map<String, Object>> response =
                surveyService.getAccessibleSurvey(null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Session token is missing.", response.getBody().get("message"));
    }

    @Test
    void getAccessibleSurvey_UnauthorizedSession() {
        when(sessionService.verifySession("badToken")).thenReturn(false);

        ResponseEntity<Map<String, Object>> response =
                surveyService.getAccessibleSurvey("badToken");
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Unauthorized or session expired.", response.getBody().get("message"));
    }

    @Test
    void getAccessibleSurvey_UserNotFound() {
        when(sessionService.verifySession("validToken")).thenReturn(true);
        when(sessionRepository.findBySessionToken("validToken"))
                .thenReturn(Optional.of(mockSession));
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> response =
                surveyService.getAccessibleSurvey("validToken");
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("User not found in database.", response.getBody().get("message"));
    }

    @Test
    void getAccessibleSurvey_Success() {
        when(sessionService.verifySession("validToken")).thenReturn(true);
        when(sessionRepository.findBySessionToken("validToken"))
                .thenReturn(Optional.of(mockSession));
        when(userRepository.findByUsername("testUser"))
                .thenReturn(Optional.of(mockUser));

        // user自己创建过1个survey
        List<Survey> userSurveys = new ArrayList<>();
        userSurveys.add(mockSurvey); // ID=1
        when(surveyRepository.findAllByUser(mockUser)).thenReturn(userSurveys);

        // access 里多1个 survey(2)
        Access a1 = new Access();
        a1.setUser(mockUser);
        Survey s2 = new Survey();
        s2.setId(2L);
        a1.setSurvey(s2);

        // 这里模仿: user对survey=2有访问权限
        List<Access> mockAccesses = new ArrayList<>();
        mockAccesses.add(a1);
        when(accessRepository.findAccessByUser(mockUser)).thenReturn(mockAccesses);

        // surveyRepository.findAllById([2]) => 取到 s2
        when(surveyRepository.findAllById(anyList())).thenReturn(Arrays.asList(s2));

        ResponseEntity<Map<String, Object>> response =
                surveyService.getAccessibleSurvey("validToken");
        assertEquals(HttpStatus.OK, response.getStatusCode());

        List<Survey> retSurveys = (List<Survey>) response.getBody().get("surveys");
        assertEquals(2, retSurveys.size());
        // 包含自己创建 ID=1, 以及通过 access 拿到的 ID=2
    }
}
