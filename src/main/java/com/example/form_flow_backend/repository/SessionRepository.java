package com.example.form_flow_backend.repository;

import com.example.form_flow_backend.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findBySessionToken(String sessionToken);
    void deleteBySessionToken(String sessionToken);
}
