package com.example.form_flow_backend.DTO;

import com.example.form_flow_backend.model.Question;

import java.util.List;

public class UpdateQuestionsRequest {
    private String surveyId;
    private List<Question> questions; // Using the entity directly

    // Getters and setters
    public String getSurveyId() {
        return surveyId;
    }
    public void setSurveyId(String surveyId) {
        this.surveyId = surveyId;
    }
    public List<Question> getQuestions() {
        return questions;
    }
    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
}
