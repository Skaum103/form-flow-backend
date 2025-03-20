package com.example.form_flow_backend.repository.Access;

import com.example.form_flow_backend.model.Access;
import com.example.form_flow_backend.model.Survey;
import com.example.form_flow_backend.model.User;
import com.example.form_flow_backend.repository.SurveyRepository;
import com.example.form_flow_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class AccessRepositoryTest {

    @Autowired
    private AccessRepository accessRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SurveyRepository surveyRepository;

    private User user;
    private Survey survey;

    @BeforeEach
    void setUp() {
        // 1. 构造一个 User，并给所有 NOT NULL 字段赋值
        user = new User();
        user.setUsername("userA");
        user.setEmail("userA@example.com");   // 必填，否则会触发 not-null 错误
        user.setPassword("testPass");         // 同理，如果 @Column(nullable = false)
        user = userRepository.save(user);

        // 2. 构造一个 Survey（如果 Survey 也有 NOT NULL 字段，要赋值）
        survey = new Survey();
        survey.setSurveyName("surveyA");
        // 若 Survey 需要绑定 user (例如 Survey 实体有 user_id not null)
        survey.setUser(user);
        // 若还有其他必填字段，如 description，请务必 set
        survey = surveyRepository.save(survey);

        // 3. 构造一个 Access
        Access access = new Access();
        access.setUser(user);
        access.setSurvey(survey);
        // 若 Access 还有其他非空字段，也要赋值
        accessRepository.save(access);
    }

    @Test
    void testSaveAndFind() {
        // 验证是否成功保存 1 条记录
        var allAccess = accessRepository.findAll();
        assertEquals(1, allAccess.size());
        assertEquals("userA", allAccess.get(0).getUser().getUsername());
        assertEquals("surveyA", allAccess.get(0).getSurvey().getSurveyName());
    }

    @Test
    void testDelete() {
        // 测试删除
        var access = accessRepository.findAll().get(0);
        accessRepository.delete(access);

        assertEquals(0, accessRepository.count());
    }

    @Test
    void testUpdate() {
        // 取出已保存的记录
        var access = accessRepository.findAll().get(0);
        // 假设 Access 有其他可修改的字段，在此示例中只演示一下重新 save
        accessRepository.save(access);

        // 确认数据库里仍是 1 条
        assertEquals(1, accessRepository.count());
    }
}
