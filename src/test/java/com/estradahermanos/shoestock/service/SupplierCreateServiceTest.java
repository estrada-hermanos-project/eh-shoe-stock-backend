package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.request.CreateSupplierRequestDTO;
import com.estradahermanos.shoestock.dto.response.SupplierResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.mapper.SupplierMapper;
import com.estradahermanos.shoestock.repository.entities.Supplier;
import com.estradahermanos.shoestock.repository.repositories.SupplierRepository;
import com.estradahermanos.shoestock.utilities.SupplierValidator;
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
class SupplierCreateServiceTest
{
    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private SupplierMapper supplierMapper;

    private SupplierCreateService service;

    @BeforeEach
    void setUp()
    {
        service = new SupplierCreateService(supplierRepository, supplierMapper, new SupplierValidator());
    }

    @Test
    void createPersistsWhenValidationsPass()
    {
        CreateSupplierRequestDTO request = CreateSupplierRequestDTO.builder()
                .fullName("Carlos Estrada")
                .phone("55512345")
                .build();
        Supplier entity = Supplier.builder().fullName(request.getFullName()).phone(request.getPhone()).build();
        SupplierResponseDTO response = SupplierResponseDTO.builder().id(5).name("Carlos Estrada").phone("55512345").build();

        when(supplierRepository.existsByPhone(request.getPhone())).thenReturn(false);
        when(supplierMapper.toEntity(request)).thenReturn(entity);
        when(supplierRepository.save(entity)).thenReturn(entity);
        when(supplierMapper.toResponse(entity)).thenReturn(response);

        SupplierResponseDTO result = service.create(request);

        assertEquals("Carlos Estrada", result.getName());
        verify(supplierRepository).save(entity);
    }

    @Test
    void createRejectsDuplicatePhone()
    {
        CreateSupplierRequestDTO request = CreateSupplierRequestDTO.builder()
                .fullName("Carlos Estrada")
                .phone("55512345")
                .build();
        when(supplierRepository.existsByPhone(request.getPhone())).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.create(request));

        assertEquals(HttpStatus.CONFLICT, exception.getCode());
        verify(supplierRepository, never()).save(any());
    }

    @Test
    void createRejectsInvalidPhone()
    {
        CreateSupplierRequestDTO request = CreateSupplierRequestDTO.builder()
                .fullName("Carlos Estrada")
                .phone("123")
                .build();

        assertThrows(BusinessException.class, () -> service.create(request));
        verify(supplierRepository, never()).existsByPhone(any());
    }

    @Test
    void createRejectsBlankFullName()
    {
        CreateSupplierRequestDTO request = CreateSupplierRequestDTO.builder()
                .fullName("")
                .phone("55512345")
                .build();

        assertThrows(BusinessException.class, () -> service.create(request));
        verify(supplierRepository, never()).existsByPhone(any());
    }
}
