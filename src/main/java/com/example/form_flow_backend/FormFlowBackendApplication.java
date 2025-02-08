package com.example.form_flow_backend;

import com.example.form_flow_backend.Utilities.SecretManagerUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for Form Flow Backend.
 * English comment: Entry point for the Spring Boot application.
 */
@SpringBootApplication
public class FormFlowBackendApplication {

	/**
	 * Application entry point.
	 *
	 * @param args command-line arguments
	 * @throws JSONException if there is an error processing JSON
	 */
	public static void main(String[] args) throws JSONException {
		System.setProperty("DEPLOY_MODE", "cloud");

		// If in local deployment mode, no secret retrieval is needed.
		if (System.getProperty("DEPLOY_MODE").equals("local")) {
			// Local deployment; no action needed.
		} else {
			// Retrieve database credentials from secret manager.
			JSONObject secret = SecretManagerUtil.getSecret(System.getenv("DB_SECRET_NAME"));
			System.setProperty("DB_USERNAME", secret.getString("username"));
			System.setProperty("DB_PASSWORD", secret.getString("password"));
		}

		SpringApplication.run(FormFlowBackendApplication.class, args);
	}
}
