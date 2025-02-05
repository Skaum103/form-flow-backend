package com.example.form_flow_backend;

import com.example.form_flow_backend.Utilities.SecretManagerUtil;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FormFlowBackendApplicationTests {

    @BeforeAll
    static void setup() {
        System.setProperty("DEPLOY_MODE", "cloud");
		System.getenv("DB_USERNAME");
        System.getenv("DB_PASSWORD");
	}

    @Test
    void contextLoads() {
    }



}
