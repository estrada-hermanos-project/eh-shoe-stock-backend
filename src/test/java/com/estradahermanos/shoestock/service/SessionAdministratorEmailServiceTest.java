package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.repository.entities.UserAccount;
import com.estradahermanos.shoestock.repository.entities.UserAdmin;
import com.estradahermanos.shoestock.repository.entities.UserSession;
import com.estradahermanos.shoestock.repository.repositories.UserAccountRepository;
import com.estradahermanos.shoestock.repository.repositories.UserAdminRepository;
import com.estradahermanos.shoestock.repository.repositories.UserSessionRepository;
import com.estradahermanos.shoestock.utilities.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionAdministratorEmailServiceTest
{
    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private UserSessionRepository userSessionRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private UserAdminRepository userAdminRepository;

    private SessionAdministratorEmailService service;

    @BeforeEach
    void setUp()
    {
        service = new SessionAdministratorEmailService(
                jwtProvider,
                userSessionRepository,
                userAccountRepository,
                userAdminRepository);
    }

    @Test
    void resolvesEmailFromBearerToken()
    {
        stubSession("jwt-token");
        when(userAdminRepository.findByDpi("1234567890123")).thenReturn(Optional.of(admin("  ana@gmail.com  ")));

        assertEquals("ana@gmail.com", service.resolve("Bearer jwt-token"));
    }

    @Test
    void resolvesEmailFromRawToken()
    {
        stubSession("jwt-token");
        when(userAdminRepository.findByDpi("1234567890123")).thenReturn(Optional.of(admin("ana@gmail.com")));

        assertEquals("ana@gmail.com", service.resolve("jwt-token"));
    }

    @Test
    void rejectsMissingToken()
    {
        BusinessException missing = assertThrows(BusinessException.class, () -> service.resolve(null));
        BusinessException blank = assertThrows(BusinessException.class, () -> service.resolve("   "));
        assertEquals("Session token is required", missing.getMessage());
        assertEquals("Session token is required", blank.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, missing.getCode());
    }

    @Test
    void rejectsInvalidJwt()
    {
        when(jwtProvider.sessionCodeFrom("bad")).thenThrow(new IllegalArgumentException("bad token"));

        BusinessException exception = assertThrows(BusinessException.class, () -> service.resolve("Bearer bad"));
        assertEquals("Invalid or expired session", exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getCode());
    }

    @Test
    void rejectsUnknownSession()
    {
        when(jwtProvider.sessionCodeFrom("jwt-token")).thenReturn("Ab12C");
        when(userSessionRepository.findBySessionCode("Ab12C")).thenReturn(Optional.empty());

        assertEquals("Invalid or expired session", assertThrows(BusinessException.class, () -> service.resolve("jwt-token"))
                .getMessage());
    }

    @Test
    void rejectsExpiredSession()
    {
        when(jwtProvider.sessionCodeFrom("jwt-token")).thenReturn("Ab12C");
        when(userSessionRepository.findBySessionCode("Ab12C")).thenReturn(Optional.of(UserSession.builder()
                .sessionCode("Ab12C")
                .userAccountId("diego")
                .expirationDate(LocalDateTime.now().minusMinutes(1))
                .build()));

        assertEquals("Invalid or expired session", assertThrows(BusinessException.class, () -> service.resolve("jwt-token"))
                .getMessage());
    }

    @Test
    void rejectsSessionWithoutExpiration()
    {
        when(jwtProvider.sessionCodeFrom("jwt-token")).thenReturn("Ab12C");
        when(userSessionRepository.findBySessionCode("Ab12C")).thenReturn(Optional.of(UserSession.builder()
                .sessionCode("Ab12C")
                .userAccountId("diego")
                .build()));

        assertEquals("Invalid or expired session", assertThrows(BusinessException.class, () -> service.resolve("jwt-token"))
                .getMessage());
    }

    @Test
    void rejectsUnknownAccount()
    {
        when(jwtProvider.sessionCodeFrom("jwt-token")).thenReturn("Ab12C");
        when(userSessionRepository.findBySessionCode("Ab12C")).thenReturn(Optional.of(openSession()));
        when(userAccountRepository.findByUsername("diego")).thenReturn(Optional.empty());

        assertEquals("Invalid or expired session", assertThrows(BusinessException.class, () -> service.resolve("jwt-token"))
                .getMessage());
    }

    @Test
    void rejectsAdministratorWithoutEmail()
    {
        stubSession("jwt-token");
        when(userAdminRepository.findByDpi("1234567890123")).thenReturn(Optional.of(admin("  ")));

        BusinessException exception = assertThrows(BusinessException.class, () -> service.resolve("jwt-token"));
        assertEquals(SessionAdministratorEmailService.MISSING_EMAIL_MESSAGE, exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
    }

    @Test
    void rejectsMissingAdministrator()
    {
        stubSession("jwt-token");
        when(userAdminRepository.findByDpi("1234567890123")).thenReturn(Optional.empty());

        assertEquals(
                SessionAdministratorEmailService.MISSING_EMAIL_MESSAGE,
                assertThrows(BusinessException.class, () -> service.resolve("jwt-token")).getMessage());
    }

    private void stubSession(String token)
    {
        when(jwtProvider.sessionCodeFrom(token)).thenReturn("Ab12C");
        when(userSessionRepository.findBySessionCode("Ab12C")).thenReturn(Optional.of(openSession()));
        when(userAccountRepository.findByUsername("diego")).thenReturn(Optional.of(UserAccount.builder()
                .username("diego")
                .dpi("1234567890123")
                .build()));
    }

    private UserSession openSession()
    {
        return UserSession.builder()
                .sessionCode("Ab12C")
                .userAccountId("diego")
                .expirationDate(LocalDateTime.now().plusHours(1))
                .build();
    }

    private UserAdmin admin(String email)
    {
        return UserAdmin.builder()
                .dpi("1234567890123")
                .fullName("Ana")
                .email(email)
                .build();
    }
}
