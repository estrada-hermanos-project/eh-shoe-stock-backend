package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.request.LoginRequestDTO;
import com.estradahermanos.shoestock.dto.response.LoginResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.repository.entities.UserAccount;
import com.estradahermanos.shoestock.repository.entities.UserSession;
import com.estradahermanos.shoestock.repository.repositories.UserAccountRepository;
import com.estradahermanos.shoestock.repository.repositories.UserSessionRepository;
import com.estradahermanos.shoestock.utilities.JwtProvider;
import com.estradahermanos.shoestock.utilities.SessionCodeGenerator;
import com.estradahermanos.shoestock.utilities.UserAccountValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserLoginServiceTest
{
    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private UserSessionRepository userSessionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SessionCodeGenerator sessionCodeGenerator;

    @Mock
    private JwtProvider jwtProvider;

    private UserLoginService service;

    @BeforeEach
    void setUp()
    {
        service = new UserLoginService(
                userAccountRepository, userSessionRepository, new UserAccountValidator(),
                passwordEncoder, sessionCodeGenerator, jwtProvider);
    }

    @Test
    void loginOpensSessionAndReturnsToken()
    {
        LoginRequestDTO request = LoginRequestDTO.builder().username("admin1").password("secret123").build();
        UserAccount account = UserAccount.builder().username("admin1").passwordHash("hashed").dpi("1234567890123").build();

        when(userAccountRepository.findByUsername("admin1")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("secret123", "hashed")).thenReturn(true);
        when(sessionCodeGenerator.generate()).thenReturn("Ab3Zx");
        when(userSessionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtProvider.generateForSession("Ab3Zx")).thenReturn("jwt-token");

        LoginResponseDTO result = service.login(request);

        assertEquals("jwt-token", result.getToken());
        verify(userSessionRepository).save(any(UserSession.class));
    }

    @Test
    void loginRejectsUnknownUsername()
    {
        LoginRequestDTO request = LoginRequestDTO.builder().username("admin1").password("secret123").build();
        when(userAccountRepository.findByUsername("admin1")).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> service.login(request));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getCode());
        verify(userSessionRepository, never()).save(any());
    }

    @Test
    void loginRejectsWrongPassword()
    {
        LoginRequestDTO request = LoginRequestDTO.builder().username("admin1").password("wrong").build();
        UserAccount account = UserAccount.builder().username("admin1").passwordHash("hashed").dpi("1234567890123").build();

        when(userAccountRepository.findByUsername("admin1")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.login(request));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getCode());
        verify(userSessionRepository, never()).save(any());
    }

    @Test
    void loginRejectsMissingField()
    {
        LoginRequestDTO request = LoginRequestDTO.builder().username("admin1").password("").build();

        BusinessException exception = assertThrows(BusinessException.class, () -> service.login(request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
        verify(userAccountRepository, never()).findByUsername(any());
    }
}
