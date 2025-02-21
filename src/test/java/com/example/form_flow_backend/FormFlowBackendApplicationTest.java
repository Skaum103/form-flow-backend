package com.example.form_flow_backend;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

import com.example.form_flow_backend.repository.UserRepository;
import com.example.form_flow_backend.service.UserManagementService;
import jakarta.validation.constraints.Null;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;

import com.example.form_flow_backend.Utilities.SecretManagerUtil;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;



@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class FormFlowBackendApplicationTest {

    @AfterEach
    public void tearDown() {
        // Clear system properties after each test
        System.clearProperty("DB_USERNAME");
        System.clearProperty("DB_PASSWORD");
    }

    /**
     * Test that when deploy.mode is "cloud" (passed as an argument) along with
     * --db.secret.name, the application retrieves credentials.
     */
    @Test
    public void testMain_cloudDeployment() {
        // Prepare a dummy secret.
        try (MockedStatic<SecretManagerUtil> secretManagerMock = mockStatic(SecretManagerUtil.class)) {
            JSONObject dummySecret = new JSONObject();
            dummySecret.put("username", "dummyUser");
            dummySecret.put("password", "dummyPass");
            secretManagerMock.when(() -> SecretManagerUtil.getSecret("dummySecret"))
                    .thenReturn(dummySecret);

            // Call main with the expected arguments.
            String[] args = {"--deploy.mode=cloud", "--server.port=8888"};
            // Set the system property for DB_SECRET_NAME before calling main.
            System.setProperty("DB_SECRET_NAME", "dummySecret");
            FormFlowBackendApplication.main(args);

            // Instead of verifying SpringApplication.run, verify the side effects:
            assertEquals("dummyUser", System.getProperty("DB_USERNAME"));
            assertEquals("dummyPass", System.getProperty("DB_PASSWORD"));
        }
    }


    /**
     * Test that when deploy.mode is "cloud" (passed as an argument) along with
     * --db.secret.name, the application retrieves credentials.
     */
    @Test
    public void testMain_cloudDeployment_NoDBSecret() {
        // Prepare a dummy secret.
        try (MockedStatic<SecretManagerUtil> secretManagerMock = mockStatic(SecretManagerUtil.class)) {
            JSONObject dummySecret = new JSONObject();
            dummySecret.put("username", "dummyUser");
            dummySecret.put("password", "dummyPass");
            secretManagerMock.when(() -> SecretManagerUtil.getSecret("dummySecret"))
                    .thenReturn(dummySecret);

            // Call main with the expected arguments.
            String[] args = {"--deploy.mode=cloud"};
            FormFlowBackendApplication.main(args);
        }
        catch (NullPointerException e) {
            // Instead of verifying SpringApplication.run, verify the side effects:
            assertNull(System.getProperty("DB_USERNAME"));
            assertNull(System.getProperty("DB_PASSWORD"));
        }
    }

    /**
     * Test that when deploy.mode is "local" (passed as an argument), the application
     * skips secret retrieval.
     */
    @Test
    public void testMain_localDeployment() throws Exception {
        String[] args = {"--deploy.mode=local", "--server.port=0"};
        // Call main; if it doesn't throw, it’s working as expected.
        FormFlowBackendApplication.main(args);

        // Verify that no DB credentials were set in local mode.
        assertNull(System.getProperty("DB_USERNAME"));
        assertNull(System.getProperty("DB_PASSWORD"));
    }

}
