package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.request.CreateShoeRequestDTO;
import com.estradahermanos.shoestock.dto.response.ShoeResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.mapper.ShoeMapper;
import com.estradahermanos.shoestock.repository.entities.Shoe;
import com.estradahermanos.shoestock.repository.entities.Supplier;
import com.estradahermanos.shoestock.repository.repositories.ShoeRepository;
import com.estradahermanos.shoestock.repository.repositories.SupplierRepository;
import com.estradahermanos.shoestock.utilities.ShoeValidator;
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
class ShoeCreateServiceTest
{
    @Mock
    private ShoeRepository shoeRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ShoeMapper shoeMapper;

    private ShoeCreateService service;

    @BeforeEach
    void setUp()
    {
        service = new ShoeCreateService(shoeRepository, supplierRepository, shoeMapper, new ShoeValidator());
    }

    private CreateShoeRequestDTO validRequest()
    {
        return CreateShoeRequestDTO.builder()
                .code("OXF-001")
                .type("Caballero")
                .name("Oxford clasico")
                .description("Zapato formal")
                .supplierId(2)
                .build();
    }

    @Test
    void createPersistsWhenValidationsPass()
    {
        CreateShoeRequestDTO request = validRequest();
        Shoe entity = Shoe.builder().code("OXF-001").type("Caballero").name("Oxford clasico").supplier(2).build();

        when(shoeRepository.existsByCode("OXF-001")).thenReturn(false);
        when(supplierRepository.findById(2)).thenReturn(Optional.of(
                Supplier.builder().id(2).fullName("Maria Lopez").phone("55512345").build()));
        when(shoeMapper.toEntity(request)).thenReturn(entity);
        when(shoeRepository.save(entity)).thenReturn(entity);
        when(shoeMapper.toResponse(any(Shoe.class), any())).thenReturn(
                ShoeResponseDTO.builder().code("OXF-001").supplierName("Maria Lopez").build());

        ShoeResponseDTO result = service.create(request);

        assertEquals("Maria Lopez", result.getSupplierName());
        verify(shoeRepository).save(entity);
        org.junit.jupiter.api.Assertions.assertNotNull(entity.getCreatedAt());
    }

    @Test
    void createRejectsDuplicateCode()
    {
        CreateShoeRequestDTO request = validRequest();
        when(shoeRepository.existsByCode("OXF-001")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.create(request));

        assertEquals(HttpStatus.CONFLICT, exception.getCode());
        verify(shoeRepository, never()).save(any());
    }

    @Test
    void createRejectsInvalidType()
    {
        CreateShoeRequestDTO request = validRequest();
        request.setType("Unisex");

        BusinessException exception = assertThrows(BusinessException.class, () -> service.create(request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
        verify(shoeRepository, never()).existsByCode(any());
    }

    @Test
    void createRejectsMissingSupplier()
    {
        CreateShoeRequestDTO request = validRequest();
        when(shoeRepository.existsByCode("OXF-001")).thenReturn(false);
        when(supplierRepository.findById(2)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> service.create(request));

        assertEquals(HttpStatus.NOT_FOUND, exception.getCode());
        verify(shoeRepository, never()).save(any());
    }
}
