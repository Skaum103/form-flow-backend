package com.example.form_flow_backend.DTO;

import com.example.form_flow_backend.model.Question;
import lombok.Getter;

import java.util.List;

@Getter
public class UpdateQuestionsRequest {
    // Getters and setters
    private String surveyId;
    private List<Question> questions; // Using the entity directly

    public void setSurveyId(String surveyId) {
        this.surveyId = surveyId;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
}
