package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.request.LoginCodeVerifyRequestDTO;
import com.estradahermanos.shoestock.dto.response.LoginResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.repository.entities.LoginCode;
import com.estradahermanos.shoestock.repository.entities.UserAccount;
import com.estradahermanos.shoestock.repository.repositories.LoginCodeRepository;
import com.estradahermanos.shoestock.repository.repositories.UserAccountRepository;
import com.estradahermanos.shoestock.utilities.BusinessDate;
import com.estradahermanos.shoestock.utilities.UserAccountValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginCodeVerifyServiceTest
{
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 20, 8, 0);

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private LoginCodeRepository loginCodeRepository;

    @Mock
    private UserLoginService userLoginService;

    @Mock
    private BusinessDate businessDate;

    private LoginCodeVerifyService service;

    @BeforeEach
    void setUp()
    {
        service = new LoginCodeVerifyService(
                userAccountRepository,
                loginCodeRepository,
                new UserAccountValidator(),
                userLoginService,
                businessDate);
    }

    @Test
    void validCodeOpensTheSameSessionAsLogin()
    {
        LoginCode loginCode = code(false, NOW.plusSeconds(1));
        UserAccount account = account();
        when(loginCodeRepository.findLatestByUsernameAndCode("diego", "1234")).thenReturn(Optional.of(loginCode));
        when(businessDate.now()).thenReturn(NOW);
        when(userAccountRepository.findByUsername("diego")).thenReturn(Optional.of(account));
        when(userLoginService.openSession(account)).thenReturn(LoginResponseDTO.builder().token("jwt-token").build());

        LoginResponseDTO result = service.verify(request("diego", " 1234 "));

        assertEquals("jwt-token", result.getToken());
        assertTrue(loginCode.isUsed());
        InOrder order = inOrder(loginCodeRepository, userLoginService);
        order.verify(loginCodeRepository).save(loginCode);
        order.verify(userLoginService).openSession(account);
    }

    @Test
    void codeInsideTheTwoMinuteWindowIsValid()
    {
        LoginCode loginCode = code(false, NOW.plusMinutes(2).minusSeconds(1));
        UserAccount account = account();
        when(loginCodeRepository.findLatestByUsernameAndCode("diego", "1234")).thenReturn(Optional.of(loginCode));
        when(businessDate.now()).thenReturn(NOW);
        when(userAccountRepository.findByUsername("diego")).thenReturn(Optional.of(account));
        when(userLoginService.openSession(account)).thenReturn(LoginResponseDTO.builder().token("jwt-token").build());

        assertEquals("jwt-token", service.verify(request("diego", "1234")).getToken());
    }

    @Test
    void rejectsACodeThatDoesNotMatch()
    {
        when(loginCodeRepository.findLatestByUsernameAndCode("diego", "1234")).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.verify(request("diego", "1234")));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getCode());
        assertEquals("El codigo es incorrecto", exception.getMessage());
        verify(userLoginService, never()).openSession(any());
        verify(loginCodeRepository, never()).save(any());
    }

    @Test
    void rejectsACodeThatBelongsToAnotherUser()
    {
        LoginCode loginCode = code(false, NOW.plusMinutes(1));
        loginCode.setUsername("otro");
        when(loginCodeRepository.findLatestByUsernameAndCode("diego", "1234")).thenReturn(Optional.of(loginCode));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.verify(request("diego", "1234")));

        assertEquals("El codigo es incorrecto", exception.getMessage());
        assertFalse(loginCode.isUsed());
        verify(userLoginService, never()).openSession(any());
    }

    @Test
    void rejectsAnExpiredCode()
    {
        LoginCode loginCode = code(false, NOW);
        when(loginCodeRepository.findLatestByUsernameAndCode("diego", "1234")).thenReturn(Optional.of(loginCode));
        when(businessDate.now()).thenReturn(NOW);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.verify(request("diego", "1234")));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
        assertEquals("El código ha expirado", exception.getMessage());
        assertFalse(loginCode.isUsed());
        verify(userLoginService, never()).openSession(any());
    }

    @Test
    void rejectsACodePastTheTwoMinuteWindow()
    {
        LoginCode loginCode = code(false, NOW.minusSeconds(1));
        when(loginCodeRepository.findLatestByUsernameAndCode("diego", "1234")).thenReturn(Optional.of(loginCode));
        when(businessDate.now()).thenReturn(NOW);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.verify(request("diego", "1234")));

        assertEquals("El código ha expirado", exception.getMessage());
        verify(userLoginService, never()).openSession(any());
    }

    @Test
    void rejectsACodeWithoutExpiration()
    {
        LoginCode loginCode = code(false, null);
        when(loginCodeRepository.findLatestByUsernameAndCode("diego", "1234")).thenReturn(Optional.of(loginCode));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.verify(request("diego", "1234")));

        assertEquals("El código ha expirado", exception.getMessage());
        verify(userLoginService, never()).openSession(any());
    }

    @Test
    void rejectsACodeAlreadyUsed()
    {
        LoginCode loginCode = code(true, NOW.plusMinutes(1));
        when(loginCodeRepository.findLatestByUsernameAndCode("diego", "1234")).thenReturn(Optional.of(loginCode));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.verify(request("diego", "1234")));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
        assertEquals("The login code was already used", exception.getMessage());
        verify(userLoginService, never()).openSession(any());
        verify(loginCodeRepository, never()).save(any());
    }

    @Test
    void rejectsAReplacedCodeEvenWhenItHasNotExpired()
    {
        LoginCode loginCode = code(true, NOW.plusMinutes(2));
        when(loginCodeRepository.findLatestByUsernameAndCode("diego", "9999")).thenReturn(Optional.of(loginCode));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.verify(request("diego", "9999")));

        assertEquals("The login code was already used", exception.getMessage());
        verify(userLoginService, never()).openSession(any());
    }

    @Test
    void rejectsACodeThatIsNotFourDigits()
    {
        List<String> codes = java.util.Arrays.asList("123", "12345", "12a4", "  ", null);
        for (String code : codes)
        {
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> service.verify(request("diego", code)));
            assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
            assertEquals("Login code must contain exactly 4 digits", exception.getMessage());
        }
        verify(loginCodeRepository, never()).findLatestByUsernameAndCode(any(), any());
    }

    @Test
    void rejectsMissingUsername()
    {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.verify(request(" ", "1234")));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
        assertEquals("Username is required", exception.getMessage());
        verify(loginCodeRepository, never()).findLatestByUsernameAndCode(any(), any());
    }

    @Test
    void rejectsMissingRequest()
    {
        BusinessException exception = assertThrows(BusinessException.class, () -> service.verify(null));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
        assertEquals("Request body is required", exception.getMessage());
    }

    @Test
    void rejectsAValidCodeWhenTheAccountNoLongerExists()
    {
        LoginCode loginCode = code(false, NOW.plusMinutes(1));
        when(loginCodeRepository.findLatestByUsernameAndCode("diego", "1234")).thenReturn(Optional.of(loginCode));
        when(businessDate.now()).thenReturn(NOW);
        when(userAccountRepository.findByUsername("diego")).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.verify(request("diego", "1234")));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getCode());
        assertEquals("Invalid credentials", exception.getMessage());
        assertFalse(loginCode.isUsed());
        verify(userLoginService, never()).openSession(any());
    }

    @Test
    void unexpectedFailureReturnsControlledError()
    {
        when(loginCodeRepository.findLatestByUsernameAndCode("diego", "1234"))
                .thenThrow(new IllegalStateException("db"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.verify(request("diego", "1234")));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getCode());
        assertEquals("Could not verify login code", exception.getMessage());
        verify(userLoginService, never()).openSession(any());
    }

    private LoginCodeVerifyRequestDTO request(String username, String code)
    {
        return LoginCodeVerifyRequestDTO.builder().username(username).code(code).build();
    }

    private UserAccount account()
    {
        return UserAccount.builder().username("diego").passwordHash("hashed").dpi("1234567890123").build();
    }

    private LoginCode code(boolean used, LocalDateTime expiration)
    {
        return LoginCode.builder()
                .id(1)
                .username("diego")
                .code("1234")
                .creationDate(LocalDateTime.of(2026, 10, 20, 7, 58))
                .expirationDate(expiration)
                .used(used)
                .build();
    }
}
