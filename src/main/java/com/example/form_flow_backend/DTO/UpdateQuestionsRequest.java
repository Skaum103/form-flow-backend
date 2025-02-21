package com.example.form_flow_backend.DTO;

import com.example.form_flow_backend.model.Question;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateQuestionsRequest {
    // Getters and setters
    private String sessionToken;
    private String surveyId;
    private List<Question> questions; // Using the entity directly

}
