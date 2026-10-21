package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.mail.MailMessage;
import com.estradahermanos.shoestock.dto.request.LoginRequestDTO;
import com.estradahermanos.shoestock.dto.response.LoginChallengeResponseDTO;
import com.estradahermanos.shoestock.dto.response.LoginResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.mapper.LoginCodeMapper;
import com.estradahermanos.shoestock.repository.entities.LoginCode;
import com.estradahermanos.shoestock.repository.entities.UserAccount;
import com.estradahermanos.shoestock.repository.entities.UserAdmin;
import com.estradahermanos.shoestock.repository.entities.UserSession;
import com.estradahermanos.shoestock.repository.repositories.LoginCodeRepository;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserLoginServiceTest
{
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 20, 8, 0);

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private UserSessionRepository userSessionRepository;

    @Mock
    private UserAdminRepository userAdminRepository;

    @Mock
    private LoginCodeRepository loginCodeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SessionCodeGenerator sessionCodeGenerator;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private LoginCodeGenerator loginCodeGenerator;

    @Mock
    private MailSendService mailSendService;

    @Mock
    private BusinessDate businessDate;

    private UserLoginService service;

    @BeforeEach
    void setUp()
    {
        service = new UserLoginService(
                userAccountRepository,
                userSessionRepository,
                userAdminRepository,
                loginCodeRepository,
                Mappers.getMapper(LoginCodeMapper.class),
                new UserAccountValidator(),
                passwordEncoder,
                sessionCodeGenerator,
                jwtProvider,
                loginCodeGenerator,
                mailSendService,
                businessDate);
    }

    @Test
    void loginSendsACodeAndDoesNotOpenASession()
    {
        LoginCode previous = LoginCode.builder().username("admin1").code("9999").used(false).build();
        when(userAccountRepository.findByUsername("admin1")).thenReturn(Optional.of(account()));
        when(passwordEncoder.matches("secret123", "hashed")).thenReturn(true);
        when(userAdminRepository.findByDpi("1234567890123")).thenReturn(Optional.of(admin("  ana@gmail.com  ")));
        when(loginCodeRepository.findUnusedByUsername("admin1")).thenReturn(List.of(previous));
        when(loginCodeGenerator.generate()).thenReturn("0042");
        when(businessDate.now()).thenReturn(NOW);
        when(loginCodeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LoginChallengeResponseDTO result = service.login(credentials("admin1", "secret123"));

        assertTrue(result.isSecondStepRequired());
        assertEquals("A verification code was sent to the administrator email", result.getMessage());
        assertTrue(previous.isUsed());
        verify(loginCodeRepository).saveAll(List.of(previous));

        ArgumentCaptor<LoginCode> saved = ArgumentCaptor.forClass(LoginCode.class);
        verify(loginCodeRepository).save(saved.capture());
        assertEquals("0042", saved.getValue().getCode());
        assertEquals("admin1", saved.getValue().getUsername());
        assertEquals(NOW, saved.getValue().getCreationDate());
        assertEquals(NOW.plusMinutes(2), saved.getValue().getExpirationDate());
        assertFalse(saved.getValue().isUsed());

        ArgumentCaptor<MailMessage> mail = ArgumentCaptor.forClass(MailMessage.class);
        verify(mailSendService).send(mail.capture());
        assertEquals("ana@gmail.com", mail.getValue().getRecipient());
        assertEquals("Código de inicio de sesión", mail.getValue().getSubject());
        assertTrue(mail.getValue().getBody().contains("0042"));
        assertTrue(mail.getValue().getBody().contains("2 minutos"));
        assertNull(mail.getValue().getAttachment());
        verify(userSessionRepository, never()).save(any());
        verify(jwtProvider, never()).generateForSession(any());
    }

    @Test
    void loginRejectsUnknownUsername()
    {
        when(userAccountRepository.findByUsername("admin1")).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.login(credentials("admin1", "secret123")));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getCode());
        assertEquals("Invalid credentials", exception.getMessage());
        verify(mailSendService, never()).send(any());
        verify(loginCodeRepository, never()).save(any());
        verify(userSessionRepository, never()).save(any());
    }

    @Test
    void loginRejectsWrongPassword()
    {
        when(userAccountRepository.findByUsername("admin1")).thenReturn(Optional.of(account()));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.login(credentials("admin1", "wrong")));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getCode());
        assertEquals("Invalid credentials", exception.getMessage());
        verify(mailSendService, never()).send(any());
        verify(loginCodeRepository, never()).save(any());
        verify(userSessionRepository, never()).save(any());
    }

    @Test
    void loginRejectsMissingField()
    {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.login(credentials("admin1", "")));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
        verify(userAccountRepository, never()).findByUsername(any());
        verify(mailSendService, never()).send(any());
    }

    @Test
    void loginRejectsMissingRequest()
    {
        BusinessException exception = assertThrows(BusinessException.class, () -> service.login(null));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
        assertEquals("Request body is required", exception.getMessage());
        verify(userAccountRepository, never()).findByUsername(any());
    }

    @Test
    void loginRejectsAdministratorWithoutEmail()
    {
        when(userAccountRepository.findByUsername("admin1")).thenReturn(Optional.of(account()));
        when(passwordEncoder.matches("secret123", "hashed")).thenReturn(true);
        when(userAdminRepository.findByDpi("1234567890123")).thenReturn(Optional.of(admin("   ")));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.login(credentials("admin1", "secret123")));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
        assertEquals("The administrator has no email registered", exception.getMessage());
        verify(mailSendService, never()).send(any());
        verify(loginCodeGenerator, never()).generate();
        verify(userSessionRepository, never()).save(any());
    }

    @Test
    void loginRejectsMissingAdministrator()
    {
        when(userAccountRepository.findByUsername("admin1")).thenReturn(Optional.of(account()));
        when(passwordEncoder.matches("secret123", "hashed")).thenReturn(true);
        when(userAdminRepository.findByDpi("1234567890123")).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.login(credentials("admin1", "secret123")));

        assertEquals("The administrator has no email registered", exception.getMessage());
        verify(mailSendService, never()).send(any());
    }

    @Test
    void loginRejectsNullEmail()
    {
        when(userAccountRepository.findByUsername("admin1")).thenReturn(Optional.of(account()));
        when(passwordEncoder.matches("secret123", "hashed")).thenReturn(true);
        when(userAdminRepository.findByDpi("1234567890123")).thenReturn(Optional.of(admin(null)));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.login(credentials("admin1", "secret123")));

        assertEquals("The administrator has no email registered", exception.getMessage());
        verify(loginCodeRepository, never()).save(any());
    }

    @Test
    void mailFailureDoesNotOpenASession()
    {
        when(userAccountRepository.findByUsername("admin1")).thenReturn(Optional.of(account()));
        when(passwordEncoder.matches("secret123", "hashed")).thenReturn(true);
        when(userAdminRepository.findByDpi("1234567890123")).thenReturn(Optional.of(admin("ana@gmail.com")));
        when(loginCodeRepository.findUnusedByUsername("admin1")).thenReturn(List.of());
        when(loginCodeGenerator.generate()).thenReturn("0042");
        when(businessDate.now()).thenReturn(NOW);
        when(loginCodeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(BusinessException.builder()
                .code(HttpStatus.BAD_GATEWAY)
                .message("Could not send email")
                .build()).when(mailSendService).send(any());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.login(credentials("admin1", "secret123")));

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getCode());
        assertEquals("Could not send email", exception.getMessage());
        verify(userSessionRepository, never()).save(any());
    }

    @Test
    void unexpectedFailureReturnsControlledError()
    {
        when(userAccountRepository.findByUsername("admin1")).thenThrow(new IllegalStateException("db"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.login(credentials("admin1", "secret123")));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getCode());
        assertEquals("Could not authenticate user", exception.getMessage());
    }

    @Test
    void openSessionPersistsOneHourAndReturnsJwt()
    {
        when(sessionCodeGenerator.generate()).thenReturn("Ab3Zx");
        when(userSessionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtProvider.generateForSession("Ab3Zx")).thenReturn("jwt-token");

        LoginResponseDTO result = service.openSession(account());

        ArgumentCaptor<UserSession> session = ArgumentCaptor.forClass(UserSession.class);
        verify(userSessionRepository).save(session.capture());
        assertEquals("jwt-token", result.getToken());
        assertEquals("Ab3Zx", session.getValue().getSessionCode());
        assertEquals("admin1", session.getValue().getUserAccountId());
        assertEquals(session.getValue().getCreationDate().plusHours(1), session.getValue().getExpirationDate());
    }

    private LoginRequestDTO credentials(String username, String password)
    {
        return LoginRequestDTO.builder().username(username).password(password).build();
    }

    private UserAccount account()
    {
        return UserAccount.builder().username("admin1").passwordHash("hashed").dpi("1234567890123").build();
    }

    private UserAdmin admin(String email)
    {
        return UserAdmin.builder()
                .dpi("1234567890123")
                .fullName("Ana Estrada")
                .phone("55555555")
                .email(email)
                .build();
    }
}
