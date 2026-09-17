package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.repository.repositories.UserAdminRepository;
import com.estradahermanos.shoestock.utilities.UserAdminValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAdminDeleteServiceTest
{
    @Mock
    private UserAdminRepository userAdminRepository;

    private UserAdminDeleteService service;

    @BeforeEach
    void setUp()
    {
        service = new UserAdminDeleteService(userAdminRepository, new UserAdminValidator());
    }

    @Test
    void deleteRemovesExistingUser()
    {
        when(userAdminRepository.existsByDpi("1234567890123")).thenReturn(true);

        service.deleteByDpi("1234567890123");

        verify(userAdminRepository).deleteByDpi("1234567890123");
    }

    @Test
    void deleteRejectsMissingUser()
    {
        when(userAdminRepository.existsByDpi("1234567890123")).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.deleteByDpi("1234567890123"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getCode());
        verify(userAdminRepository, never()).deleteByDpi("1234567890123");
    }
}
