package com.example.form_flow_backend.repository.Access;

import com.example.form_flow_backend.model.Access;
import com.example.form_flow_backend.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class AccessRepositoryCustomImpl implements AccessRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Access> findAccessByUser(User user) {
        String jpql = "SELECT a FROM Access a WHERE a.user.id = :userId OR a.user.id = -1";
        TypedQuery<Access> query = entityManager.createQuery(jpql, Access.class);
        query.setParameter("userId", user.getId());
        return query.getResultList();
    }
}
