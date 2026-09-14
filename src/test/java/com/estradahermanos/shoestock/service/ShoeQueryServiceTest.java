package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.response.ShoeResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.mapper.ShoeMapper;
import com.estradahermanos.shoestock.repository.entities.Shoe;
import com.estradahermanos.shoestock.repository.entities.Supplier;
import com.estradahermanos.shoestock.repository.repositories.ShoeRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShoeQueryServiceTest
{
    @Mock
    private ShoeRepository shoeRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ShoeMapper shoeMapper;

    private ShoeQueryService service;

    @BeforeEach
    void setUp()
    {
        service = new ShoeQueryService(shoeRepository, supplierRepository, shoeMapper);
    }

    @Test
    void findByCodeReturnsShoe()
    {
        Shoe shoe = Shoe.builder().code("OXF-001").type("Caballero").name("Oxford").supplier(2).build();
        when(shoeRepository.findByCode("OXF-001")).thenReturn(Optional.of(shoe));
        when(supplierRepository.findById(2)).thenReturn(Optional.of(
                Supplier.builder().id(2).fullName("Maria Lopez").phone("55512345").build()));
        when(shoeMapper.toResponse(any(Shoe.class), any())).thenReturn(
                ShoeResponseDTO.builder().code("OXF-001").supplierName("Maria Lopez").build());

        ShoeResponseDTO result = service.findByCode("OXF-001");

        assertEquals("OXF-001", result.getCode());
    }

    @Test
    void findByCodeRejectsMissingShoe()
    {
        when(shoeRepository.findByCode("OXF-001")).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> service.findByCode("OXF-001"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getCode());
    }

    @Test
    void findAllReturnsList()
    {
        Shoe shoe = Shoe.builder().code("OXF-001").type("Caballero").name("Oxford").supplier(2).build();
        when(supplierRepository.findAll()).thenReturn(List.of(
                Supplier.builder().id(2).fullName("Maria Lopez").phone("55512345").build()));
        when(shoeRepository.findAll()).thenReturn(List.of(shoe));
        when(shoeMapper.toResponse(any(Shoe.class), any())).thenReturn(
                ShoeResponseDTO.builder().code("OXF-001").supplierName("Maria Lopez").build());

        List<ShoeResponseDTO> result = service.findAll();

        assertEquals(1, result.size());
    }

    @Test
    void findBySupplierRejectsMissingSupplier()
    {
        when(supplierRepository.findById(2)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> service.findBySupplier(2));

        assertEquals(HttpStatus.NOT_FOUND, exception.getCode());
    }

    @Test
    void findBySupplierReturnsList()
    {
        Shoe shoe = Shoe.builder().code("OXF-001").type("Caballero").name("Oxford").supplier(2).build();
        when(supplierRepository.findById(2)).thenReturn(Optional.of(
                Supplier.builder().id(2).fullName("Maria Lopez").phone("55512345").build()));
        when(shoeRepository.findBySupplier(2)).thenReturn(List.of(shoe));
        when(shoeMapper.toResponse(any(Shoe.class), any())).thenReturn(
                ShoeResponseDTO.builder().code("OXF-001").supplierName("Maria Lopez").build());

        List<ShoeResponseDTO> result = service.findBySupplier(2);

        assertEquals(1, result.size());
    }
}
