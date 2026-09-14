package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.response.StockQueryResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.mapper.ShoeStockMapper;
import com.estradahermanos.shoestock.repository.entities.Shoe;
import com.estradahermanos.shoestock.repository.entities.ShoeStock;
import com.estradahermanos.shoestock.repository.repositories.ShoeRepository;
import com.estradahermanos.shoestock.repository.repositories.ShoeStockRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryQueryServiceTest
{
    @Mock
    private ShoeRepository shoeRepository;

    @Mock
    private ShoeStockRepository shoeStockRepository;

    @Mock
    private ShoeStockMapper shoeStockMapper;

    private InventoryQueryService service;

    @BeforeEach
    void setUp()
    {
        service = new InventoryQueryService(shoeRepository, shoeStockRepository, shoeStockMapper);
    }

    @Test
    void findStockReturnsVariantWithShoeName()
    {
        ShoeStock variant = ShoeStock.builder().id(7).shoeId("OXF-001").color("Negro").size(40).stock(12).minStock(2).build();
        when(shoeStockRepository.findByShoeIdAndColorAndSize("OXF-001", "Negro", 40)).thenReturn(Optional.of(variant));
        when(shoeRepository.findByCode("OXF-001")).thenReturn(Optional.of(
                Shoe.builder().code("OXF-001").name("Oxford clasico").build()));
        when(shoeStockMapper.toStockQuery(any(), any())).thenReturn(
                StockQueryResponseDTO.builder().name("Oxford clasico").color("Negro").size(40).stock(12).build());

        StockQueryResponseDTO result = service.findStock("OXF-001", "Negro", 40);

        assertEquals("Oxford clasico", result.getName());
        assertEquals(12, result.getStock());
    }

    @Test
    void findStockRejectsMissingVariant()
    {
        when(shoeStockRepository.findByShoeIdAndColorAndSize("OXF-001", "Negro", 40)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> service.findStock("OXF-001", "Negro", 40));

        assertEquals(HttpStatus.NOT_FOUND, exception.getCode());
    }
}
