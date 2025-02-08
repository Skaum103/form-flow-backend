package com.example.form_flow_backend.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class OpenAPIConfiguration {

    /**
     * Configures the OpenAPI specification for the backend API.
     *
     * @return the configured OpenAPI instance
     */
    @Bean
    public OpenAPI defineOpenApi() {
        Server server = new Server();

        // Set server URL based on deployment mode
        if (System.getProperty("DEPLOY_MODE").equals("local")) {
            server.setUrl("http://localhost:8080/");
        } else {
            server.setUrl("http://form-flow.us-east-1.elasticbeanstalk.com/");
        }
        server.setDescription("API Gateway Base URL");

        Contact myContact = new Contact();
        myContact.setName("Meng Xin");
        myContact.setEmail("your.email@gmail.com");

        Info information = new Info()
                .title("Form Flow Backend API")
                .version("1.0")
                .description("This API exposes endpoints to manage forms.")
                .contact(myContact);

        return new OpenAPI().info(information)
                .servers(Collections.singletonList(server));
    }
}
