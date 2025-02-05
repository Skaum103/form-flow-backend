package com.example.form_flow_backend;

import com.example.form_flow_backend.Utilities.SecretManagerUtil;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FormFlowBackendApplicationTests {

    private static final Logger logger = LoggerFactory.getLogger(FormFlowBackendApplicationTests.class);
    @Value("${spring.datasource.url:NOT_SET}")
    private static String datasourceUrl;

    @BeforeAll
    static void setup() {
        System.setProperty("DEPLOY_MODE", "cloud");
        System.setProperty("AWS_REGION", "us-east-1");
        JSONObject secret = SecretManagerUtil.getSecret(System.getenv("DB_SECRET_NAME"));
        System.setProperty("DB_USERNAME", secret.getString("username"));
        System.setProperty("DB_PASSWORD", secret.getString("password"));

	}

    @Test
    void contextLoads() {
    }



}
