package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.request.CreateUserAdminRequestDTO;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAdminCreateServiceTest
{
    @Mock
    private UserAdminRepository userAdminRepository;

    @Mock
    private UserAdminMapper userAdminMapper;

    private UserAdminCreateService service;

    @BeforeEach
    void setUp()
    {
        service = new UserAdminCreateService(userAdminRepository, userAdminMapper, new UserAdminValidator());
    }

    @Test
    void createPersistsWhenValidationsPass()
    {
        CreateUserAdminRequestDTO request = CreateUserAdminRequestDTO.builder()
                .dpi("1234567890123")
                .fullName("Ana Lopez")
                .phone("55551234")
                .email("ana@gmail.com")
                .build();
        UserAdmin entity  = UserAdmin.builder().dpi(request.getDpi()).fullName(request.getFullName()).phone(request.getPhone()).build();
        UserAdminResponseDTO response = UserAdminResponseDTO.builder().name("Ana Lopez").phone("55551234").email("ana@gmail.com").build();

        when(userAdminRepository.existsByDpi(request.getDpi())).thenReturn(false);
        when(userAdminMapper.toEntity(request)).thenReturn(entity);
        when(userAdminRepository.save(entity)).thenReturn(entity);
        when(userAdminMapper.toResponse(entity)).thenReturn(response);

        UserAdminResponseDTO result = service.create(request);

        assertEquals("Ana Lopez", result.getName());
        verify(userAdminRepository).save(entity);
    }

    @Test
    void createRejectsDuplicateDpi()
    {
        CreateUserAdminRequestDTO request = CreateUserAdminRequestDTO.builder()
                .dpi("1234567890123")
                .fullName("Ana Lopez")
                .phone("55551234")
                .build();
        when(userAdminRepository.existsByDpi(request.getDpi())).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.create(request));

        assertEquals(HttpStatus.CONFLICT, exception.getCode());
        verify(userAdminRepository, never()).save(any());
    }

    @Test
    void createRejectsInvalidEmailDomain()
    {
        CreateUserAdminRequestDTO request = CreateUserAdminRequestDTO.builder()
                .dpi("1234567890123")
                .fullName("Ana Lopez")
                .phone("55551234")
                .email("ana@yahoo.com")
                .build();

        assertThrows(BusinessException.class, () -> service.create(request));
        verify(userAdminRepository, never()).existsByDpi(any());
    }
}
