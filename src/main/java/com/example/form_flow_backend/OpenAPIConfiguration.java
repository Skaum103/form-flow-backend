package com.example.form_flow_backend;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

@Configuration
public class OpenAPIConfiguration {

    @Bean
    public OpenAPI defineOpenApi() {
        Server server = new Server();
        server.setUrl("https://7inj6cbj3i.execute-api.us-east-1.amazonaws.com/Prod"); // Use API Gateway URL if available
        server.setDescription("API Gateway Base URL");

        Contact myContact = new Contact();
        myContact.setName("Meng Xin");
        myContact.setEmail("your.email@gmail.com");

        Info information = new Info()
                .title("Form Flow Backend API")
                .version("1.0")
                .description("This API exposes endpoints to manage forms.")
                .contact(myContact);

        return new OpenAPI().info(information).servers(Collections.singletonList(server));
    }
}