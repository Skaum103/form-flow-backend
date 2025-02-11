package com.example.form_flow_backend;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import com.example.form_flow_backend.Utilities.SecretManagerUtil;


@SpringBootApplication
public class FormFlowBackendApplication {

	public static void main(String[] args) throws JSONException {
		// Use SpringApplicationBuilder to configure the application
		SpringApplicationBuilder builder = new SpringApplicationBuilder(FormFlowBackendApplication.class);

		// Add an initializer to read the deploy mode and conditionally retrieve secrets.
		builder.initializers((ApplicationContextInitializer<ConfigurableApplicationContext>) applicationContext -> {
			Environment env = applicationContext.getEnvironment();
			// Command-line arguments are automatically bound as properties.
			String deployMode = env.getProperty("deploy.mode", "cloud");

			if ("local".equalsIgnoreCase(deployMode)) {
				// Local deployment: no need to retrieve AWS secrets.
				System.out.println("Running in local mode. Skipping AWS secret retrieval.");
			} else {
				// Cloud deployment: retrieve database credentials from AWS Secrets Manager.
				String dbSecretName = env.getProperty("DB_SECRET_NAME");
				if (dbSecretName != null) {
					JSONObject secret = SecretManagerUtil.getSecret(dbSecretName);
					System.setProperty("DB_USERNAME", secret.getString("username"));
					System.setProperty("DB_PASSWORD", secret.getString("password"));
					System.out.println("Retrieved DB credentials from AWS Secrets Manager.");
				} else {
					System.err.println("db.secret.name property not provided!");
				}
			}
		});

		// Run the application with the given command-line arguments.
		builder.run(args);
	}
}
