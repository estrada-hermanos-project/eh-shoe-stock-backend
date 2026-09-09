package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.repository.repositories.SupplierRepository;
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
class SupplierDeleteServiceTest
{
    @Mock
    private SupplierRepository supplierRepository;

    private SupplierDeleteService service;

    @BeforeEach
    void setUp()
    {
        service = new SupplierDeleteService(supplierRepository);
    }

    @Test
    void deleteRemovesExistingSupplier()
    {
        when(supplierRepository.existsById(5)).thenReturn(true);

        service.deleteById(5);

        verify(supplierRepository).deleteById(5);
    }

    @Test
    void deleteRejectsMissingSupplier()
    {
        when(supplierRepository.existsById(5)).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.deleteById(5));

        assertEquals(HttpStatus.NOT_FOUND, exception.getCode());
        verify(supplierRepository, never()).deleteById(5);
    }
}
