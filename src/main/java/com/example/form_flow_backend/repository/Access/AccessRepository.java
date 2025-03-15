package com.example.form_flow_backend.repository.Access;

import com.example.form_flow_backend.model.Access;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessRepository extends JpaRepository<Access, Long>, AccessRepositoryCustom {

}
