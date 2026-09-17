package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.request.CreateUserAccountRequestDTO;
import com.estradahermanos.shoestock.dto.response.UserAccountResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.mapper.UserAccountMapper;
import com.estradahermanos.shoestock.repository.entities.UserAccount;
import com.estradahermanos.shoestock.repository.repositories.UserAccountRepository;
import com.estradahermanos.shoestock.repository.repositories.UserAdminRepository;
import com.estradahermanos.shoestock.utilities.UserAccountValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAccountCreateServiceTest
{
    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private UserAdminRepository userAdminRepository;

    @Mock
    private UserAccountMapper userAccountMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserAccountCreateService service;

    @BeforeEach
    void setUp()
    {
        service = new UserAccountCreateService(
                userAccountRepository, userAdminRepository, userAccountMapper,
                new UserAccountValidator(), passwordEncoder);
    }

    @Test
    void createPersistsWhenValidationsPass()
    {
        CreateUserAccountRequestDTO request = CreateUserAccountRequestDTO.builder()
                .username("admin1")
                .password("secret123")
                .dpi("1234567890123")
                .build();

        when(userAccountRepository.existsByUsername("admin1")).thenReturn(false);
        when(userAdminRepository.existsByDpi("1234567890123")).thenReturn(true);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed");
        when(userAccountRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userAccountMapper.toResponse(any())).thenReturn(
                UserAccountResponseDTO.builder().username("admin1").dpi("1234567890123").build());

        UserAccountResponseDTO result = service.create(request);

        assertEquals("admin1", result.getUsername());
        verify(userAccountRepository).save(any(UserAccount.class));
    }

    @Test
    void createRejectsDuplicateUsername()
    {
        CreateUserAccountRequestDTO request = CreateUserAccountRequestDTO.builder()
                .username("admin1")
                .password("secret123")
                .dpi("1234567890123")
                .build();
        when(userAccountRepository.existsByUsername("admin1")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.create(request));

        assertEquals(HttpStatus.CONFLICT, exception.getCode());
        verify(userAccountRepository, never()).save(any());
    }

    @Test
    void createRejectsWhenAdminDpiDoesNotExist()
    {
        CreateUserAccountRequestDTO request = CreateUserAccountRequestDTO.builder()
                .username("admin1")
                .password("secret123")
                .dpi("1234567890123")
                .build();
        when(userAccountRepository.existsByUsername("admin1")).thenReturn(false);
        when(userAdminRepository.existsByDpi("1234567890123")).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.create(request));

        assertEquals(HttpStatus.NOT_FOUND, exception.getCode());
        verify(userAccountRepository, never()).save(any());
    }

    @Test
    void createRejectsMissingField()
    {
        CreateUserAccountRequestDTO request = CreateUserAccountRequestDTO.builder()
                .username("admin1")
                .password("")
                .dpi("1234567890123")
                .build();

        BusinessException exception = assertThrows(BusinessException.class, () -> service.create(request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
        verify(userAccountRepository, never()).existsByUsername(any());
    }
}
