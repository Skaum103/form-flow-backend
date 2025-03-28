package com.example.form_flow_backend.repository;

import com.example.form_flow_backend.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    Optional<List<Question>> findBySurveyId(Long surveyId);
    @Transactional
    void deleteBySurveyId(Long surveyId);
}
