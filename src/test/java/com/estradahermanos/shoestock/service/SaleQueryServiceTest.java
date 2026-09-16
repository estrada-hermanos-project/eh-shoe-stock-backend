package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.response.SaleResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.mapper.SaleMapper;
import com.estradahermanos.shoestock.repository.entities.Sale;
import com.estradahermanos.shoestock.repository.entities.Shoe;
import com.estradahermanos.shoestock.repository.entities.ShoeStock;
import com.estradahermanos.shoestock.repository.repositories.SaleRepository;
import com.estradahermanos.shoestock.repository.repositories.ShoeRepository;
import com.estradahermanos.shoestock.repository.repositories.ShoeStockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaleQueryServiceTest
{
    @Mock
    private SaleRepository saleRepository;

    @Mock
    private ShoeStockRepository shoeStockRepository;

    @Mock
    private ShoeRepository shoeRepository;

    @Mock
    private SaleMapper saleMapper;

    private SaleQueryService service;

    @BeforeEach
    void setUp()
    {
        service = new SaleQueryService(saleRepository, shoeStockRepository, shoeRepository, saleMapper);
    }

    private Sale sale()
    {
        return Sale.builder()
                .id(1)
                .shoeStockId(7)
                .amount(2)
                .size(40)
                .saleDate(LocalDate.of(2026, 9, 14))
                .build();
    }

    private void stubResolution()
    {
        when(shoeStockRepository.findAllById(anyCollection())).thenReturn(List.of(
                ShoeStock.builder().id(7).shoeId("OXF-001").color("Negro").size(40).stock(8).minStock(0).build()));
        when(shoeRepository.findAllById(anyCollection())).thenReturn(List.of(
                Shoe.builder().code("OXF-001").type("Caballero").name("Oxford clasico").supplier(2).build()));
        when(saleMapper.toResponse(any(), any(), any())).thenReturn(
                SaleResponseDTO.builder()
                        .size(40)
                        .name("Oxford clasico")
                        .color("Negro")
                        .stock(2)
                        .saleDate(LocalDate.of(2026, 9, 14))
                        .build());
    }

    @Test
    void findByDateRangeReturnsSales()
    {
        LocalDate start = LocalDate.of(2026, 9, 1);
        LocalDate end   = LocalDate.of(2026, 9, 14);
        when(saleRepository.findBySaleDateBetween(start, end)).thenReturn(List.of(sale()));
        stubResolution();

        List<SaleResponseDTO> result = service.findByDateRange(start, end);

        assertEquals(1, result.size());
        assertEquals("Oxford clasico", result.get(0).getName());
        assertEquals("Negro", result.get(0).getColor());
    }

    @Test
    void findByDateRangeRejectsInvertedDates()
    {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.findByDateRange(LocalDate.of(2026, 9, 14), LocalDate.of(2026, 9, 1)));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
        assertEquals("start_date must not be after end_date", exception.getMessage());
        verify(saleRepository, never()).findBySaleDateBetween(any(), any());
    }

    @Test
    void findByDateRangeReturnsEmptyList()
    {
        LocalDate start = LocalDate.of(2026, 9, 1);
        LocalDate end   = LocalDate.of(2026, 9, 14);
        when(saleRepository.findBySaleDateBetween(start, end)).thenReturn(List.of());

        List<SaleResponseDTO> result = service.findByDateRange(start, end);

        assertTrue(result.isEmpty());
        verify(shoeStockRepository, never()).findAllById(anyCollection());
    }

    @Test
    void findByDateReturnsSales()
    {
        LocalDate date = LocalDate.of(2026, 9, 14);
        when(saleRepository.findBySaleDate(date)).thenReturn(List.of(sale()));
        stubResolution();

        List<SaleResponseDTO> result = service.findByDate(date);

        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getStock());
    }
}
