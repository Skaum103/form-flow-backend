package com.example.form_flow_backend.repository;

import com.example.form_flow_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<User, Long> {
}
