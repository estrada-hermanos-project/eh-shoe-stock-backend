package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.request.LoginRequestDTO;
import com.estradahermanos.shoestock.dto.request.PasswordResetRequestDTO;
import com.estradahermanos.shoestock.dto.response.LoginChallengeResponseDTO;
import com.estradahermanos.shoestock.dto.response.MessageResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.mapper.LoginCodeMapper;
import com.estradahermanos.shoestock.repository.entities.PasswordRecoveryToken;
import com.estradahermanos.shoestock.repository.entities.UserAccount;
import com.estradahermanos.shoestock.repository.entities.UserAdmin;
import com.estradahermanos.shoestock.repository.repositories.LoginCodeRepository;
import com.estradahermanos.shoestock.repository.repositories.PasswordRecoveryTokenRepository;
import com.estradahermanos.shoestock.repository.repositories.UserAccountRepository;
import com.estradahermanos.shoestock.repository.repositories.UserAdminRepository;
import com.estradahermanos.shoestock.repository.repositories.UserSessionRepository;
import com.estradahermanos.shoestock.utilities.BusinessDate;
import com.estradahermanos.shoestock.utilities.JwtProvider;
import com.estradahermanos.shoestock.utilities.LoginCodeGenerator;
import com.estradahermanos.shoestock.utilities.SessionCodeGenerator;
import com.estradahermanos.shoestock.utilities.UserAccountValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest
{
    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private PasswordRecoveryTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private PasswordResetService service;

    @BeforeEach
    void setUp()
    {
        service = new PasswordResetService(
                userAccountRepository, tokenRepository, new UserAccountValidator(), passwordEncoder);
    }

    @Test
    void storesBcryptHashAndConsumesTheToken()
    {
        UserAccount account = account("old-hash");
        PasswordRecoveryToken token = openToken("diego");
        when(tokenRepository.findByToken("raw-token")).thenReturn(Optional.of(token));
        when(userAccountRepository.findByUsername("diego")).thenReturn(Optional.of(account));
        when(passwordEncoder.encode("new-secret")).thenReturn("new-hash");

        MessageResponseDTO response = service.reset(resetRequest("diego", "new-secret", "new-secret"));

        assertEquals("Password updated", response.getMessage());
        assertEquals("new-hash", account.getPasswordHash());
        assertTrue(token.isUsed());
        verify(userAccountRepository).save(account);
        verify(tokenRepository).save(token);
    }

    @Test
    void loginAcceptsOnlyTheNewPassword()
    {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        UserAccount account = account(encoder.encode("old-secret"));
        PasswordRecoveryToken token = openToken("diego");
        when(tokenRepository.findByToken("raw-token")).thenReturn(Optional.of(token));
        when(userAccountRepository.findByUsername("diego")).thenReturn(Optional.of(account));
        PasswordResetService resetService = new PasswordResetService(
                userAccountRepository, tokenRepository, new UserAccountValidator(), encoder);

        resetService.reset(resetRequest("diego", "new-secret", "new-secret"));

        assertFalse(encoder.matches("old-secret", account.getPasswordHash()));
        assertTrue(encoder.matches("new-secret", account.getPasswordHash()));

        UserSessionRepository sessionRepository = mock(UserSessionRepository.class);
        UserAdminRepository adminRepository = mock(UserAdminRepository.class);
        LoginCodeRepository loginCodeRepository = mock(LoginCodeRepository.class);
        LoginCodeGenerator loginCodeGenerator = mock(LoginCodeGenerator.class);
        MailSendService mailSendService = mock(MailSendService.class);
        BusinessDate businessDate = mock(BusinessDate.class);
        when(adminRepository.findByDpi("1234567890123")).thenReturn(Optional.of(UserAdmin.builder()
                .dpi("1234567890123")
                .fullName("Diego Estrada")
                .phone("55555555")
                .email("diego@mail.com")
                .build()));
        when(loginCodeRepository.findUnusedByUsername("diego")).thenReturn(List.of());
        when(loginCodeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(loginCodeGenerator.generate()).thenReturn("1234");
        when(businessDate.now()).thenReturn(LocalDateTime.of(2026, 10, 20, 8, 0));
        UserLoginService loginService = new UserLoginService(
                userAccountRepository,
                sessionRepository,
                adminRepository,
                loginCodeRepository,
                Mappers.getMapper(LoginCodeMapper.class),
                new UserAccountValidator(),
                encoder,
                mock(SessionCodeGenerator.class),
                mock(JwtProvider.class),
                loginCodeGenerator,
                mailSendService,
                businessDate);

        BusinessException rejected = assertThrows(BusinessException.class, () -> loginService.login(
                LoginRequestDTO.builder().username("diego").password("old-secret").build()));
        assertEquals(HttpStatus.UNAUTHORIZED, rejected.getCode());
        assertEquals("Invalid credentials", rejected.getMessage());

        LoginChallengeResponseDTO challenge = loginService.login(
                LoginRequestDTO.builder().username("diego").password("new-secret").build());
        assertTrue(challenge.isSecondStepRequired());
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void rejectsPasswordMismatchWithoutChangingIt()
    {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.reset(resetRequest("diego", "new-secret", "other-secret")));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
        assertEquals("Password confirmation does not match", exception.getMessage());
        verify(passwordEncoder, never()).encode(any());
        verify(tokenRepository, never()).findByToken(any());
    }

    @Test
    void rejectsEmptyPassword()
    {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.reset(resetRequest("diego", "  ", "  ")));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
        assertEquals("Password is required", exception.getMessage());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void rejectsEmptyConfirmation()
    {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.reset(resetRequest("diego", "new-secret", " ")));

        assertEquals("Password confirmation is required", exception.getMessage());
    }

    @Test
    void rejectsEmptyToken()
    {
        BusinessException exception = assertThrows(BusinessException.class, () -> service.reset(
                PasswordResetRequestDTO.builder()
                        .username("diego")
                        .newPassword("new-secret")
                        .confirmPassword("new-secret")
                        .token(" ")
                        .build()));

        assertEquals("Recovery token is required", exception.getMessage());
    }

    @Test
    void rejectsMissingBody()
    {
        BusinessException exception = assertThrows(BusinessException.class, () -> service.reset(null));

        assertEquals("Request body is required", exception.getMessage());
    }

    @Test
    void rejectsExpiredToken()
    {
        PasswordRecoveryToken token = openToken("diego");
        token.setExpirationDate(LocalDateTime.now(BusinessDate.ZONE).minusMinutes(1));
        when(tokenRepository.findByToken("raw-token")).thenReturn(Optional.of(token));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.reset(resetRequest("diego", "new-secret", "new-secret")));

        assertEquals("Recovery token is invalid or expired", exception.getMessage());
        assertFalse(token.isUsed());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void rejectsUsedToken()
    {
        PasswordRecoveryToken token = openToken("diego");
        token.setUsed(true);
        when(tokenRepository.findByToken("raw-token")).thenReturn(Optional.of(token));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.reset(resetRequest("diego", "new-secret", "new-secret")));

        assertEquals("Recovery token is invalid or expired", exception.getMessage());
        verify(userAccountRepository, never()).save(any());
    }

    @Test
    void rejectsTokenOfAnotherUser()
    {
        when(tokenRepository.findByToken("raw-token")).thenReturn(Optional.of(openToken("otro")));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.reset(resetRequest("diego", "new-secret", "new-secret")));

        assertEquals("Recovery token is invalid or expired", exception.getMessage());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void rejectsUnknownToken()
    {
        when(tokenRepository.findByToken("raw-token")).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.reset(resetRequest("diego", "new-secret", "new-secret")));

        assertEquals("Recovery token is invalid or expired", exception.getMessage());
    }

    @Test
    void rejectsTokenWithoutExpiration()
    {
        PasswordRecoveryToken token = openToken("diego");
        token.setExpirationDate(null);
        when(tokenRepository.findByToken("raw-token")).thenReturn(Optional.of(token));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.reset(resetRequest("diego", "new-secret", "new-secret")));

        assertEquals("Recovery token is invalid or expired", exception.getMessage());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void rejectsMissingAccountWithoutConsumingTheToken()
    {
        PasswordRecoveryToken token = openToken("diego");
        when(tokenRepository.findByToken("raw-token")).thenReturn(Optional.of(token));
        when(userAccountRepository.findByUsername("diego")).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.reset(resetRequest("diego", "new-secret", "new-secret")));

        assertEquals("Recovery token is invalid or expired", exception.getMessage());
        assertFalse(token.isUsed());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void unexpectedFailureReturnsControlledError()
    {
        when(tokenRepository.findByToken("raw-token")).thenThrow(new IllegalStateException("db"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.reset(resetRequest("diego", "new-secret", "new-secret")));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getCode());
        assertEquals("Could not update password", exception.getMessage());
    }

    private UserAccount account(String passwordHash)
    {
        return UserAccount.builder().username("diego").passwordHash(passwordHash).dpi("1234567890123").build();
    }

    private PasswordRecoveryToken openToken(String username)
    {
        return PasswordRecoveryToken.builder()
                .id(4)
                .token("raw-token")
                .username(username)
                .creationDate(LocalDateTime.now(BusinessDate.ZONE).minusMinutes(1))
                .expirationDate(LocalDateTime.now(BusinessDate.ZONE).plusMinutes(29))
                .used(false)
                .build();
    }

    private PasswordResetRequestDTO resetRequest(String username, String password, String confirmation)
    {
        return PasswordResetRequestDTO.builder()
                .username(username)
                .newPassword(password)
                .confirmPassword(confirmation)
                .token("raw-token")
                .build();
    }
}
