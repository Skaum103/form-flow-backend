package com.example.form_flow_backend.repository;

import com.example.form_flow_backend.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
}
