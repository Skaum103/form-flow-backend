package com.example.form_flow_backend.repository;

import com.example.form_flow_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findByUsernameIn(Collection<String> usernames);
    Optional<User> findByEmail(String email);
}
