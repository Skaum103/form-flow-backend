package com.example.form_flow_backend;

import com.example.form_flow_backend.Utilities.SecretManagerUtil;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
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
    static void setUp() {
		System.setProperty("DEPLOY_MODE", "cloud");

		if (System.getProperty("DEPLOY_MODE").equals("local")) {
		} else {
			JSONObject secret = SecretManagerUtil.getSecret(System.getenv("DB_SECRET_NAME"));
			System.setProperty("DB_USERNAME", secret.getString("username"));
			System.setProperty("DB_PASSWORD", secret.getString("password"));
		}
	}

	@Test
	void contextLoads() {
	}
}
