package com.example.form_flow_backend.service;

import com.example.form_flow_backend.model.Survey;
import com.example.form_flow_backend.model.User;
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

@ExtendWith(SpringExtension.class) // JUnit 5 + Spring
class SurveyServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private SurveyRepository surveyRepository;

    @InjectMocks
    private SurveyService surveyService;  // 被测类，自动注入上面两个Mock

    @BeforeEach
    void setUp() {
        // 可以在这里做一些初始化操作
        // 也可以用 @Mock 的时候直接在每个 test 内部写 when thenReturn
    }

    @Test
    void createSurvey_Success() {
        // 1. 准备模拟参数
        String loggedInUsername = "testUser";
        Map<String, String> requestData = new HashMap<>();
        requestData.put("username", loggedInUsername);
        requestData.put("surveyName", "My First Survey");
        requestData.put("description", "Just a test.");

        // 2. 模拟数据库返回
        User fakeUser = new User();
        fakeUser.setUsername("testUser");
        fakeUser.setId(1L);
        when(userRepository.findByUsername(loggedInUsername))
                .thenReturn(Optional.of(fakeUser));

        // 3. 模拟保存 Survey 的行为
        Survey savedSurvey = new Survey();
        savedSurvey.setId(100L);
        savedSurvey.setSurveyName("My First Survey");
        when(surveyRepository.save(any(Survey.class))).thenReturn(savedSurvey);

        // 4. 调用被测方法
        ResponseEntity<Map<String, Object>> response = surveyService.createSurvey(requestData);

        // 5. 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals("Survey created successfully", response.getBody().get("message"));
        assertEquals(100L, response.getBody().get("surveyId"));

        // 6. 验证 Mock 的调用是否符合预期
        verify(userRepository, times(1)).findByUsername(loggedInUsername);
        verify(surveyRepository, times(1)).save(any(Survey.class));
    }

    @Test
    void createSurvey_UserNotFound() {
        // 1. 准备
        String loggedInUsername = "nonExistentUser";
        Map<String, String> requestData = new HashMap<>();
        requestData.put("username", loggedInUsername);
        requestData.put("surveyName", "Some Survey");

        // 2. 模拟：查不到用户
        when(userRepository.findByUsername(loggedInUsername))
                .thenReturn(Optional.empty());

        // 3. 调用
        ResponseEntity<Map<String, Object>> response = surveyService.createSurvey(requestData);

        // 4. 验证
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("User not found in database", response.getBody().get("message"));

        // 5. 验证 repository 调用
        verify(userRepository, times(1)).findByUsername(loggedInUsername);
        verify(surveyRepository, never()).save(any(Survey.class));
    }

    @Test
    void createSurvey_SurveyNameMissing() {
        // 1. 准备
        String loggedInUsername = "testUser";
        Map<String, String> requestData = new HashMap<>();
        // 故意不放 surveyName
        requestData.put("username", loggedInUsername);
        requestData.put("description", "Just a test.");

        User fakeUser = new User();
        fakeUser.setUsername(loggedInUsername);
        when(userRepository.findByUsername(loggedInUsername))
                .thenReturn(Optional.of(fakeUser));

        // 2. 调用
        ResponseEntity<Map<String, Object>> response = surveyService.createSurvey(requestData);

        // 3. 验证
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("surveyName is required", response.getBody().get("message"));

        // 4. 验证 repository
        verify(surveyRepository, never()).save(any(Survey.class));
    }
}
