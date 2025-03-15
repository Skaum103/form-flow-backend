package com.example.form_flow_backend.repository;

import com.example.form_flow_backend.model.Survey;
import com.example.form_flow_backend.model.Takes;
import com.example.form_flow_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TakesRepository extends JpaRepository<Takes, Long> {
    List<Takes> findTakesBySurveyId(Long surveyId);
    Optional<Takes> findTakesBySurveyIdAndUser(Long surveyId, User user);
}
