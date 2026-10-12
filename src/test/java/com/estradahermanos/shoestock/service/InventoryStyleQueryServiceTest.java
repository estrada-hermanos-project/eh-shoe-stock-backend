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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryStyleQueryServiceTest
{
    @Mock
    private ShoeRepository shoeRepository;

    @Mock
    private ShoeStockRepository shoeStockRepository;

    @Mock
    private ShoeStockMapper shoeStockMapper;

    private InventoryStyleQueryService service;

    @BeforeEach
    void setUp()
    {
        service = new InventoryStyleQueryService(shoeRepository, shoeStockRepository, shoeStockMapper);
    }

    @Test
    void findByShoeCodeReturnsEachVariant()
    {
        Shoe shoe = Shoe.builder().code("OXF-001").name("Oxford clasico").build();
        ShoeStock black = ShoeStock.builder().shoeId("OXF-001").color("Negro").size(40).stock(12).build();
        ShoeStock brown = ShoeStock.builder().shoeId("OXF-001").color("Cafe").size(41).stock(3).build();
        when(shoeRepository.findByCode("OXF-001")).thenReturn(Optional.of(shoe));
        when(shoeStockRepository.findByShoeId("OXF-001")).thenReturn(List.of(black, brown));
        when(shoeStockMapper.toStockQuery(black, "Oxford clasico")).thenReturn(
                StockQueryResponseDTO.builder().name("Oxford clasico").color("Negro").size(40).stock(12).build());
        when(shoeStockMapper.toStockQuery(brown, "Oxford clasico")).thenReturn(
                StockQueryResponseDTO.builder().name("Oxford clasico").color("Cafe").size(41).stock(3).build());

        List<StockQueryResponseDTO> result = service.findByShoeCode("OXF-001", null, null);

        assertEquals(2, result.size());
        assertEquals("Negro", result.get(0).getColor());
        assertEquals(40, result.get(0).getSize());
        assertEquals(12, result.get(0).getStock());
        assertEquals(3, result.get(1).getStock());
    }

    @Test
    void findByShoeCodeReturnsEmptyWhenStyleHasNoVariants()
    {
        when(shoeRepository.findByCode("OXF-001")).thenReturn(Optional.of(
                Shoe.builder().code("OXF-001").name("Oxford clasico").build()));
        when(shoeStockRepository.findByShoeId("OXF-001")).thenReturn(List.of());

        List<StockQueryResponseDTO> result = service.findByShoeCode("OXF-001", null, null);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByShoeCodeRejectsUnknownStyle()
    {
        when(shoeRepository.findByCode("OXF-001")).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.findByShoeCode("OXF-001", null, null));

        assertEquals(HttpStatus.NOT_FOUND, exception.getCode());
        assertEquals("Shoe not found", exception.getMessage());
    }

    @Test
    void findByShoeCodeWrapsUnexpectedErrors()
    {
        when(shoeRepository.findByCode("OXF-001")).thenThrow(new RuntimeException("db"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.findByShoeCode("OXF-001", null, null));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getCode());
    }

    @Test
    void findByShoeCodeFiltersByColor()
    {
        ShoeStock black = ShoeStock.builder().shoeId("OXF-001").color("Negro").size(40).stock(12).build();
        when(shoeRepository.findByCode("OXF-001")).thenReturn(Optional.of(
                Shoe.builder().code("OXF-001").name("Oxford clasico").build()));
        when(shoeStockRepository.findByShoeIdAndColor("OXF-001", "Negro")).thenReturn(List.of(black));
        when(shoeStockMapper.toStockQuery(black, "Oxford clasico")).thenReturn(
                StockQueryResponseDTO.builder().name("Oxford clasico").color("Negro").size(40).stock(12).build());

        List<StockQueryResponseDTO> result = service.findByShoeCode("OXF-001", " Negro ", null);

        assertEquals(1, result.size());
        assertEquals("Negro", result.get(0).getColor());
        assertEquals(40, result.get(0).getSize());
    }

    @Test
    void findByShoeCodeFiltersBySize()
    {
        ShoeStock brown = ShoeStock.builder().shoeId("OXF-001").color("Cafe").size(41).stock(3).build();
        when(shoeRepository.findByCode("OXF-001")).thenReturn(Optional.of(
                Shoe.builder().code("OXF-001").name("Oxford clasico").build()));
        when(shoeStockRepository.findByShoeIdAndSize("OXF-001", 41)).thenReturn(List.of(brown));
        when(shoeStockMapper.toStockQuery(brown, "Oxford clasico")).thenReturn(
                StockQueryResponseDTO.builder().name("Oxford clasico").color("Cafe").size(41).stock(3).build());

        List<StockQueryResponseDTO> result = service.findByShoeCode("OXF-001", null, 41);

        assertEquals(1, result.size());
        assertEquals("Cafe", result.get(0).getColor());
        assertEquals(41, result.get(0).getSize());
    }

    @Test
    void findByShoeCodeFiltersByColorAndSize()
    {
        ShoeStock black = ShoeStock.builder().shoeId("OXF-001").color("Negro").size(40).stock(12).build();
        when(shoeRepository.findByCode("OXF-001")).thenReturn(Optional.of(
                Shoe.builder().code("OXF-001").name("Oxford clasico").build()));
        when(shoeStockRepository.findByShoeIdAndColorAndSize("OXF-001", "Negro", 40)).thenReturn(Optional.of(black));
        when(shoeStockMapper.toStockQuery(black, "Oxford clasico")).thenReturn(
                StockQueryResponseDTO.builder().name("Oxford clasico").color("Negro").size(40).stock(12).build());

        List<StockQueryResponseDTO> result = service.findByShoeCode("OXF-001", "Negro", 40);

        assertEquals(1, result.size());
        assertEquals(12, result.get(0).getStock());
    }

    @Test
    void findByShoeCodeReturnsEmptyWhenFiltersMatchNothing()
    {
        when(shoeRepository.findByCode("OXF-001")).thenReturn(Optional.of(
                Shoe.builder().code("OXF-001").name("Oxford clasico").build()));
        when(shoeStockRepository.findByShoeIdAndColorAndSize("OXF-001", "Negro", 40)).thenReturn(Optional.empty());

        List<StockQueryResponseDTO> result = service.findByShoeCode("OXF-001", "Negro", 40);

        assertTrue(result.isEmpty());
    }
}
