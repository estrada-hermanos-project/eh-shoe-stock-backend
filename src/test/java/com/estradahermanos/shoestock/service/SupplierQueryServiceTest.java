package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.response.SupplierResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.mapper.SupplierMapper;
import com.estradahermanos.shoestock.repository.entities.Supplier;
import com.estradahermanos.shoestock.repository.repositories.SupplierRepository;
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
class SupplierQueryServiceTest
{
    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private SupplierMapper supplierMapper;

    private SupplierQueryService service;

    @BeforeEach
    void setUp()
    {
        service = new SupplierQueryService(supplierRepository, supplierMapper);
    }

    @Test
    void findByIdReturnsSupplier()
    {
        Supplier supplier = Supplier.builder().id(5).fullName("Carlos Estrada").phone("55512345").build();
        SupplierResponseDTO response = SupplierResponseDTO.builder().id(5).name("Carlos Estrada").phone("55512345").build();

        when(supplierRepository.findById(5)).thenReturn(Optional.of(supplier));
        when(supplierMapper.toResponse(supplier)).thenReturn(response);

        SupplierResponseDTO result = service.findById(5);

        assertEquals("Carlos Estrada", result.getName());
    }

    @Test
    void findByIdRejectsMissingSupplier()
    {
        when(supplierRepository.findById(5)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> service.findById(5));

        assertEquals(HttpStatus.NOT_FOUND, exception.getCode());
    }

    @Test
    void findAllReturnsList()
    {
        Supplier supplier = Supplier.builder().id(5).fullName("Carlos Estrada").phone("55512345").build();
        when(supplierRepository.findAll()).thenReturn(List.of(supplier));
        when(supplierMapper.toResponseList(List.of(supplier))).thenReturn(
                List.of(SupplierResponseDTO.builder().id(5).name("Carlos Estrada").phone("55512345").build()));

        List<SupplierResponseDTO> result = service.findAll();

        assertEquals(1, result.size());
        assertEquals("Carlos Estrada", result.get(0).getName());
    }
}
