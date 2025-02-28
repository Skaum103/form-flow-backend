package com.example.form_flow_backend.service;

import com.example.form_flow_backend.model.Session;
import com.example.form_flow_backend.repository.SessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SessionServiceTest {

    @InjectMocks
    private SessionService sessionService;

    @Mock
    private SessionRepository sessionRepository;

    @Test
    public void testCreateSession() {
        String username = "testUser";

        // Stub the repository.save method to return the session passed to it.
        when(sessionRepository.save(any(Session.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Session session = sessionService.createSession(username);

        // Assertions to ensure session properties are set as expected.
        assertNotNull(session);
        assertEquals(username, session.getUsername());
        assertNotNull(session.getSessionToken());
        // Check that the expiration date is in the future.
        assertTrue(session.getExpirationDate().after(new Date()));

        verify(sessionRepository, times(1)).save(any(Session.class));
    }

    @Test
    public void testDeleteSession() {
        String token = "some-token";
        sessionService.deleteSession(token);
        verify(sessionRepository, times(1)).deleteBySessionToken(token);
    }

    @Test
    public void testGetSession_Found() {
        String token = "existing-token";
        Session session = new Session();
        session.setSessionToken(token);
        session.setUsername("testUser");
        session.setExpirationDate(new Date(System.currentTimeMillis() + 10000)); // Expires in 10 seconds

        when(sessionRepository.findBySessionToken(token)).thenReturn(Optional.of(session));

        Session found = sessionService.getSession(token);
        assertNotNull(found);
        assertEquals(token, found.getSessionToken());
        assertEquals("testUser", found.getUsername());
    }

    @Test
    public void testGetSession_NotFound() {
        String token = "nonexistent-token";
        when(sessionRepository.findBySessionToken(token)).thenReturn(Optional.empty());

        Session found = sessionService.getSession(token);
        assertNull(found);
    }

    @Test
    public void testVerifySession_Valid() {
        String token = "valid-token";
        // Create a session that expires in the future.
        Session session = new Session();
        session.setSessionToken(token);
        session.setUsername("testUser");
        session.setExpirationDate(new Date(System.currentTimeMillis() + 10000)); // Expires in 10 seconds

        when(sessionRepository.findBySessionToken(token)).thenReturn(Optional.of(session));

        boolean valid = sessionService.verifySession(token);
        assertTrue(valid);
    }

    @Test
    public void testVerifySession_Expired() {
        String token = "expired-token";
        // Create a session that expired in the past.
        Session session = new Session();
        session.setSessionToken(token);
        session.setUsername("testUser");
        session.setExpirationDate(new Date(System.currentTimeMillis() - 10000)); // Expired 10 seconds ago

        when(sessionRepository.findBySessionToken(token)).thenReturn(Optional.of(session));

        boolean valid = sessionService.verifySession(token);
        assertFalse(valid);
    }

    @Test
    public void testVerifySession_NotFound() {
        String token = "nonexistent-token";
        when(sessionRepository.findBySessionToken(token)).thenReturn(Optional.empty());

        boolean valid = sessionService.verifySession(token);
        assertFalse(valid);
    }
}
