package com.example.form_flow_backend.service;

import com.example.form_flow_backend.model.Session;
import com.example.form_flow_backend.repository.SessionRepository;
import com.example.form_flow_backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class SessionService {
    private final SessionRepository sessionRepository;

    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public Session createSession(String username) {
        // 生成一个随机的 sessionToken
        String sessionToken = UUID.randomUUID().toString();
        // 生成一个过期时间
        Date expirationDate = new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24);
        // 创建一个 Session 对象
        Session session = new Session();
        session.setSessionToken(sessionToken);
        session.setUsername(username);
        session.setExpirationDate(expirationDate);
        // 保存到数据库
        return sessionRepository.save(session);
    }

    public void deleteSession(String sessionToken) {
        sessionRepository.deleteBySessionToken(sessionToken);
    }

    public Session getSession(String sessionToken) {
        return sessionRepository.findBySessionToken(sessionToken).orElse(null);
    }

    public boolean verifySession(String sessionToken) {
        Session session = getSession(sessionToken);
        if (session == null) {
            return false;
        }
        return session.getExpirationDate().after(new Date());
    }
}
