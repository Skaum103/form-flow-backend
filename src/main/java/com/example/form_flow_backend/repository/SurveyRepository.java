package com.example.form_flow_backend.repository;

import com.example.form_flow_backend.model.Survey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyRepository extends JpaRepository<Survey, Long> {
}
