package com.example.form_flow_backend.repository.Access;

import com.example.form_flow_backend.model.Access;
import com.example.form_flow_backend.model.User;

import java.util.List;

public interface AccessRepositoryCustom {
    List<Access> findAccessByUser(User user);
}
