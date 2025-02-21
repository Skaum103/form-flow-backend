package com.example.form_flow_backend.DTO;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateSurveyRequest {
    private String sessionToken;
    private String surveyName;
    private String description;
}
