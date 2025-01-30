package com.example.form_flow_backend;

import com.example.form_flow_backend.Utilities.SecretManagerUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class FormFlowBackendApplication {

	public static void main(String[] args) throws JSONException {
		System.setProperty("DEPLOY_MODE", "local");

		if (System.getProperty("DEPLOY_MODE").equals("local")) {
			System.setProperty("DB_USERNAME", "admin");
			System.setProperty("DB_PASSWORD", "password");
		} else {
			JSONObject secret = SecretManagerUtil.getSecret(System.getenv("DB_SECRET_NAME"));
			System.setProperty("DB_USERNAME", secret.getString("username"));
			System.setProperty("DB_PASSWORD", secret.getString("password"));
		}

		SpringApplication.run(FormFlowBackendApplication.class, args);
	}

}
