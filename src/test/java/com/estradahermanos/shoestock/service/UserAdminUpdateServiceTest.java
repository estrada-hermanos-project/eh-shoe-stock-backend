package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.request.UpdateUserAdminRequestDTO;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAdminUpdateServiceTest
{
    @Mock
    private UserAdminRepository userAdminRepository;

    @Mock
    private UserAdminMapper userAdminMapper;

    private UserAdminUpdateService service;

    @BeforeEach
    void setUp()
    {
        service = new UserAdminUpdateService(userAdminRepository, userAdminMapper, new UserAdminValidator());
    }

    @Test
    void updatePersistsWhenUserExists()
    {
        UpdateUserAdminRequestDTO request = UpdateUserAdminRequestDTO.builder()
                .fullName("Ana Lopez")
                .phone("55551234")
                .build();
        UserAdmin entity = UserAdmin.builder().dpi("1234567890123").fullName("Old").phone("11111111").build();
        UserAdminResponseDTO response = UserAdminResponseDTO.builder().name("Ana Lopez").phone("55551234").build();

        when(userAdminRepository.findByDpi("1234567890123")).thenReturn(Optional.of(entity));
        when(userAdminRepository.save(entity)).thenReturn(entity);
        when(userAdminMapper.toResponse(entity)).thenReturn(response);

        UserAdminResponseDTO result = service.update("1234567890123", request);

        assertEquals("Ana Lopez", result.getName());
        verify(userAdminMapper).updateEntity(request, entity);
        verify(userAdminRepository).save(entity);
    }

    @Test
    void updateRejectsMissingUser()
    {
        UpdateUserAdminRequestDTO request = UpdateUserAdminRequestDTO.builder()
                .fullName("Ana Lopez")
                .phone("55551234")
                .build();
        when(userAdminRepository.findByDpi("1234567890123")).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> service.update("1234567890123", request));

        assertEquals(HttpStatus.NOT_FOUND, exception.getCode());
        verify(userAdminRepository, never()).save(any());
    }
}
