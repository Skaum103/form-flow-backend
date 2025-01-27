package com.example.form_flow_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class FormFlowBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(FormFlowBackendApplication.class, args);
	}

}
