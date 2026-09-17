package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.request.UpdateSupplierRequestDTO;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplierUpdateServiceTest
{
    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private SupplierMapper supplierMapper;

    private SupplierUpdateService service;

    @BeforeEach
    void setUp()
    {
        service = new SupplierUpdateService(supplierRepository, supplierMapper, new SupplierValidator());
    }

    @Test
    void updatePersistsWhenValidationsPass()
    {
        UpdateSupplierRequestDTO request = UpdateSupplierRequestDTO.builder()
                .fullName("Carlos Estrada")
                .phone("55512345")
                .build();
        Supplier supplier = Supplier.builder().id(5).fullName("Old Name").phone("55599999").build();

        when(supplierRepository.findById(5)).thenReturn(Optional.of(supplier));
        when(supplierRepository.existsByPhoneAndIdNot(request.getPhone(), 5)).thenReturn(false);
        when(supplierRepository.save(supplier)).thenReturn(supplier);
        when(supplierMapper.toResponse(supplier)).thenReturn(
                SupplierResponseDTO.builder().id(5).name("Carlos Estrada").phone("55512345").build());

        SupplierResponseDTO result = service.update(5, request);

        assertEquals("55512345", result.getPhone());
        verify(supplierRepository).save(supplier);
    }

    @Test
    void updateRejectsMissingSupplier()
    {
        UpdateSupplierRequestDTO request = UpdateSupplierRequestDTO.builder()
                .fullName("Carlos Estrada")
                .phone("55512345")
                .build();
        when(supplierRepository.findById(5)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> service.update(5, request));

        assertEquals(HttpStatus.NOT_FOUND, exception.getCode());
        verify(supplierRepository, never()).save(any());
    }

    @Test
    void updateRejectsPhoneOwnedByAnotherSupplier()
    {
        UpdateSupplierRequestDTO request = UpdateSupplierRequestDTO.builder()
                .fullName("Carlos Estrada")
                .phone("55512345")
                .build();
        Supplier supplier = Supplier.builder().id(5).fullName("Old Name").phone("55599999").build();

        when(supplierRepository.findById(5)).thenReturn(Optional.of(supplier));
        when(supplierRepository.existsByPhoneAndIdNot(request.getPhone(), 5)).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.update(5, request));

        assertEquals(HttpStatus.CONFLICT, exception.getCode());
        verify(supplierRepository, never()).save(any());
    }
}
