package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.request.SalesReportFilterDTO;
import com.estradahermanos.shoestock.dto.response.ProductSalesRankDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.mapper.ReportSalesMapper;
import com.estradahermanos.shoestock.repository.entities.Shoe;
import com.estradahermanos.shoestock.repository.entities.ShoeStock;
import com.estradahermanos.shoestock.repository.projections.SaleAmountByStockView;
import com.estradahermanos.shoestock.repository.repositories.SaleRepository;
import com.estradahermanos.shoestock.repository.repositories.ShoeRepository;
import com.estradahermanos.shoestock.utilities.ReportCatalogLookup;
import com.estradahermanos.shoestock.utilities.ReportValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportSalesQueryServiceTest
{
    @Mock
    private SaleRepository saleRepository;

    @Mock
    private ShoeRepository shoeRepository;

    @Mock
    private ReportCatalogLookup catalogLookup;

    @Mock
    private ReportSalesMapper reportSalesMapper;

    private ReportSalesQueryService service;

    @BeforeEach
    void setUp()
    {
        service = new ReportSalesQueryService(
                saleRepository, shoeRepository, catalogLookup, new ReportValidator(), reportSalesMapper);
    }

    @Test
    void topProductsOrdersByAmountDescendingAndAppliesLimit()
    {
        ShoeStock first  = ShoeStock.builder().id(1).shoeId("OXF-001").color("Negro").size(42).stock(4).build();
        ShoeStock second = ShoeStock.builder().id(2).shoeId("OXF-001").color("Cafe").size(40).stock(2).build();
        when(saleRepository.sumAmountGroupedByStock(null, null)).thenReturn(List.of(
                stockAmount(2, 3L), stockAmount(1, 10L)));
        when(catalogLookup.variantsById(anyCollection())).thenReturn(Map.of(1, first, 2, second));
        when(catalogLookup.shoesByCode(anyCollection())).thenReturn(Map.of(
                "OXF-001", Shoe.builder().code("OXF-001").name("Oxford").supplier(1).type("Caballero").build()));
        when(saleRepository.findLastSaleDateGrouped()).thenReturn(List.of());
        when(reportSalesMapper.toRank(eq(first), any(), eq(10L), eq(4), any()))
                .thenReturn(ProductSalesRankDTO.builder().shoeStockId(1).amountSold(10L).build());

        List<ProductSalesRankDTO> result = service.topProducts(SalesReportFilterDTO.builder().limit(1).build());

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getShoeStockId());
        assertEquals(10L, result.get(0).getAmountSold());
    }

    @Test
    void byPeriodRejectsMissingDates()
    {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.byPeriod(SalesReportFilterDTO.builder().build()));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
        assertEquals("start_date and end_date are required", exception.getMessage());
    }

    @Test
    void bySizeRejectsUnknownShoe()
    {
        when(shoeRepository.existsByCode("NOPE")).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.bySize(SalesReportFilterDTO.builder().shoeCode("NOPE").build()));

        assertEquals(HttpStatus.NOT_FOUND, exception.getCode());
        assertEquals("Shoe not found", exception.getMessage());
    }

    @Test
    void volumeGroupsByDay()
    {
        when(saleRepository.findBySaleDateBetween(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 2)))
                .thenReturn(List.of(
                        sale(5, LocalDate.of(2026, 9, 1)),
                        sale(3, LocalDate.of(2026, 9, 1)),
                        sale(2, LocalDate.of(2026, 9, 2))));

        var result = service.volume(SalesReportFilterDTO.builder()
                .startDate(LocalDate.of(2026, 9, 1))
                .endDate(LocalDate.of(2026, 9, 2))
                .groupBy("DAY")
                .build());

        assertEquals(2, result.size());
        assertEquals("2026-09-01", result.get(0).getPeriod());
        assertEquals(8L, result.get(0).getAmountSold());
        assertEquals(2L, result.get(0).getSaleCount());
    }

    private SaleAmountByStockView stockAmount(Integer id, Long amount)
    {
        return new SaleAmountByStockView()
        {
            @Override
            public Integer getShoeStockId()
            {
                return id;
            }

            @Override
            public Long getTotalAmount()
            {
                return amount;
            }
        };
    }

    private com.estradahermanos.shoestock.repository.entities.Sale sale(int amount, LocalDate date)
    {
        return com.estradahermanos.shoestock.repository.entities.Sale.builder()
                .id(1)
                .shoeStockId(15)
                .amount(amount)
                .size(42)
                .saleDate(date)
                .build();
    }
}
