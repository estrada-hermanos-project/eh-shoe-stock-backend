package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.response.UserAdminResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.mapper.UserAdminMapper;
import com.estradahermanos.shoestock.repository.entities.UserAdmin;
import com.estradahermanos.shoestock.repository.repositories.UserAdminRepository;
import com.estradahermanos.shoestock.utilities.UserAdminValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAdminQueryServiceTest
{
    @Mock
    private UserAdminRepository userAdminRepository;

    @Mock
    private UserAdminMapper userAdminMapper;

    private UserAdminQueryService service;

    @BeforeEach
    void setUp()
    {
        service = new UserAdminQueryService(userAdminRepository, userAdminMapper, new UserAdminValidator());
    }

    @Test
    void findByDpiReturnsMappedUser()
    {
        UserAdmin entity = UserAdmin.builder().dpi("1234567890123").fullName("Ana Lopez").phone("55551234").build();
        UserAdminResponseDTO response = UserAdminResponseDTO.builder().name("Ana Lopez").phone("55551234").build();
        when(userAdminRepository.findByDpi("1234567890123")).thenReturn(Optional.of(entity));
        when(userAdminMapper.toResponse(entity)).thenReturn(response);

        UserAdminResponseDTO result = service.findByDpi("1234567890123");

        assertEquals("Ana Lopez", result.getName());
    }

    @Test
    void findByDpiThrowsWhenMissing()
    {
        when(userAdminRepository.findByDpi("1234567890123")).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> service.findByDpi("1234567890123"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getCode());
    }

    @Test
    void findAllReturnsMappedList()
    {
        List<UserAdmin> entities = List.of(UserAdmin.builder().dpi("1234567890123").fullName("Ana").phone("55551234").build());
        List<UserAdminResponseDTO> responses = List.of(UserAdminResponseDTO.builder().name("Ana").phone("55551234").build());
        when(userAdminRepository.findAll()).thenReturn(entities);
        when(userAdminMapper.toResponseList(entities)).thenReturn(responses);

        assertEquals(1, service.findAll().size());
    }
}
