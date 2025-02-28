package com.example.form_flow_backend.repository;

import com.example.form_flow_backend.model.Survey;
import com.example.form_flow_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SurveyRepository extends JpaRepository<Survey, Long> {
    List<Survey> findAllByUser(User user);
}
