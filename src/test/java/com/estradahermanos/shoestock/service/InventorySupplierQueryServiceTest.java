package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.response.SupplierVariantResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.mapper.ShoeStockMapper;
import com.estradahermanos.shoestock.repository.entities.Shoe;
import com.estradahermanos.shoestock.repository.entities.ShoeStock;
import com.estradahermanos.shoestock.repository.repositories.ShoeRepository;
import com.estradahermanos.shoestock.repository.repositories.ShoeStockRepository;
import com.estradahermanos.shoestock.repository.repositories.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventorySupplierQueryServiceTest
{
    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ShoeRepository shoeRepository;

    @Mock
    private ShoeStockRepository shoeStockRepository;

    @Mock
    private ShoeStockMapper shoeStockMapper;

    private InventorySupplierQueryService service;

    @BeforeEach
    void setUp()
    {
        service = new InventorySupplierQueryService(
                supplierRepository, shoeRepository, shoeStockRepository, shoeStockMapper);
    }

    @Test
    void findBySupplierReturnsOnlyThatSuppliersVariants()
    {
        Shoe shoe = Shoe.builder().code("OXF-001").name("Oxford clasico").supplier(2).build();
        ShoeStock variant = ShoeStock.builder().id(15).shoeId("OXF-001").color("Negro").size(40).stock(0).build();
        when(supplierRepository.existsById(2)).thenReturn(true);
        when(shoeRepository.findBySupplier(2)).thenReturn(List.of(shoe));
        when(shoeStockRepository.findByShoeIdIn(Set.of("OXF-001"))).thenReturn(List.of(variant));
        when(shoeStockMapper.toSupplierVariant(variant, "Oxford clasico")).thenReturn(
                SupplierVariantResponseDTO.builder()
                        .shoeStockId(15)
                        .shoeCode("OXF-001")
                        .shoeName("Oxford clasico")
                        .color("Negro")
                        .size(40)
                        .build());

        List<SupplierVariantResponseDTO> result = service.findBySupplier(2);

        assertEquals(1, result.size());
        assertEquals("OXF-001", result.get(0).getShoeCode());
        assertEquals(15, result.get(0).getShoeStockId());
    }

    @Test
    void findBySupplierReturnsEmptyWhenTheShoemakerHasNoStyles()
    {
        when(supplierRepository.existsById(2)).thenReturn(true);
        when(shoeRepository.findBySupplier(2)).thenReturn(List.of());

        assertTrue(service.findBySupplier(2).isEmpty());
        verify(shoeStockRepository, never()).findByShoeIdIn(any());
    }

    @Test
    void findBySupplierRejectsUnknownSupplier()
    {
        when(supplierRepository.existsById(9)).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.findBySupplier(9));

        assertEquals(HttpStatus.NOT_FOUND, exception.getCode());
        assertEquals("Supplier not found", exception.getMessage());
        verify(shoeRepository, never()).findBySupplier(eq(9));
    }
}
