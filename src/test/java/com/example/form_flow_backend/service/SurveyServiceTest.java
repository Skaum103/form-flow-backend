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
        // 每个测试执行前都会先执行这里，你可以在此做一些通用的初始化或 Mock 行为
    }

    /**
     * 测试：创建问卷成功
     * 条件：
     * 1. sessionToken 对应的 Session 存在
     * 2. Session 中 username 对应的 User 存在
     * 3. surveyName 不为空
     */
    @Test
    void createSurvey_Success() {
        // 1. 构造请求
        CreateSurveyRequest request = new CreateSurveyRequest();
        request.setSessionToken("valid-session-token");
        request.setSurveyName("My First Survey");
        request.setDescription("Just a test.");

        // 2. Mock Session
        Session fakeSession = new Session();
        fakeSession.setSessionToken("valid-session-token");
        fakeSession.setUsername("testUser");
        when(sessionRepository.findBySessionToken("valid-session-token"))
                .thenReturn(Optional.of(fakeSession));

        // 3. Mock User
        User fakeUser = new User();
        fakeUser.setId(1L);
        fakeUser.setUsername("testUser");
        when(userRepository.findByUsername("testUser"))
                .thenReturn(Optional.of(fakeUser));

        // 4. Mock SurveyRepository 保存行为
        Survey savedSurvey = new Survey();
        savedSurvey.setId(100L);
        savedSurvey.setSurveyName("My First Survey");
        when(surveyRepository.save(any(Survey.class))).thenReturn(savedSurvey);

        // 5. 调用被测方法
        ResponseEntity<Map<String, Object>> response = surveyService.createSurvey(request);

        // 6. 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals("Survey created successfully.", response.getBody().get("message"));
        assertEquals(100L, response.getBody().get("surveyId"));

        // 7. 验证调用次数
        verify(sessionRepository, times(1)).findBySessionToken("valid-session-token");
        verify(userRepository, times(1)).findByUsername("testUser");
        verify(surveyRepository, times(1)).save(any(Survey.class));
        verifyNoMoreInteractions(sessionRepository, userRepository, surveyRepository);
    }

    /**
     * 测试：sessionToken 无效或没找到 session
     */
    @Test
    void createSurvey_InvalidSessionToken() {
        // 1. 构造请求
        CreateSurveyRequest request = new CreateSurveyRequest();
        request.setSessionToken("invalid-token");
        request.setSurveyName("SurveyName");

        // 2. Mock sessionRepository 返回空
        when(sessionRepository.findBySessionToken("invalid-token"))
                .thenReturn(Optional.empty());

        // 3. 调用
        ResponseEntity<Map<String, Object>> response = surveyService.createSurvey(request);

        // 4. 验证
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Session not found or invalid.", response.getBody().get("message"));

        // 5. 验证调用次数
        verify(sessionRepository, times(1)).findBySessionToken("invalid-token");
        verifyNoMoreInteractions(sessionRepository, userRepository, surveyRepository);
    }

    /**
     * 测试：Session 存在，但用户不存在
     */
    @Test
    void createSurvey_UserNotFound() {
        // 1. 构造请求
        CreateSurveyRequest request = new CreateSurveyRequest();
        request.setSessionToken("valid-session-token");
        request.setSurveyName("SurveyName");

        // 2. Mock Session
        Session fakeSession = new Session();
        fakeSession.setSessionToken("valid-session-token");
        fakeSession.setUsername("nonExistentUser");
        when(sessionRepository.findBySessionToken("valid-session-token"))
                .thenReturn(Optional.of(fakeSession));

        // 3. 模拟 UserRepository 查不到用户
        when(userRepository.findByUsername("nonExistentUser"))
                .thenReturn(Optional.empty());

        // 4. 调用
        ResponseEntity<Map<String, Object>> response = surveyService.createSurvey(request);

        // 5. 验证
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("User not found in database.", response.getBody().get("message"));

        // 6. 验证调用
        verify(sessionRepository, times(1)).findBySessionToken("valid-session-token");
        verify(userRepository, times(1)).findByUsername("nonExistentUser");
        verifyNoMoreInteractions(sessionRepository, userRepository, surveyRepository);
    }

    /**
     * 测试：缺少必填字段 surveyName
     */
    @Test
    void createSurvey_SurveyNameMissing() {
        // 1. 构造请求
        CreateSurveyRequest request = new CreateSurveyRequest();
        request.setSessionToken("valid-session-token");
        // 故意不设置 surveyName
        request.setDescription("Some description.");

        // 2. Mock Session, 假设 sessionToken 合法
        Session fakeSession = new Session();
        fakeSession.setSessionToken("valid-session-token");
        fakeSession.setUsername("testUser");
        when(sessionRepository.findBySessionToken("valid-session-token"))
                .thenReturn(Optional.of(fakeSession));

        // 3. Mock 找到用户
        User fakeUser = new User();
        fakeUser.setId(1L);
        fakeUser.setUsername("testUser");
        when(userRepository.findByUsername("testUser"))
                .thenReturn(Optional.of(fakeUser));

        // 4. 调用
        ResponseEntity<Map<String, Object>> response = surveyService.createSurvey(request);

        // 5. 验证
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Survey name is required.", response.getBody().get("message"));

        // 6. 验证调用
        verify(sessionRepository, times(1)).findBySessionToken("valid-session-token");
        verify(userRepository, times(1)).findByUsername("testUser");
        // 没有调用 surveyRepository.save，因为已经在校验阶段就返回了
        verifyNoMoreInteractions(sessionRepository, userRepository, surveyRepository);
    }
}
