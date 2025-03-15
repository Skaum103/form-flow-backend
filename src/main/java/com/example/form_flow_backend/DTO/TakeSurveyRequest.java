package com.example.form_flow_backend.DTO;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TakeSurveyRequest {
    private String sessionToken;
    private String surveyId;
    private String answers;
}
