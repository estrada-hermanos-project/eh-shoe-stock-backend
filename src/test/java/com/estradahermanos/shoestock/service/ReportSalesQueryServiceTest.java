package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.request.SalesReportFilterDTO;
import com.estradahermanos.shoestock.dto.response.PeriodSaleDTO;
import com.estradahermanos.shoestock.dto.response.ProductSalesRankDTO;
import com.estradahermanos.shoestock.dto.response.SalesByCategoryDTO;
import com.estradahermanos.shoestock.dto.response.SalesBySizeDTO;
import com.estradahermanos.shoestock.dto.response.SalesBySupplierDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.mapper.ReportSalesMapper;
import com.estradahermanos.shoestock.repository.entities.Sale;
import com.estradahermanos.shoestock.repository.entities.Shoe;
import com.estradahermanos.shoestock.repository.entities.ShoeStock;
import com.estradahermanos.shoestock.repository.projections.LastSaleDateView;
import com.estradahermanos.shoestock.repository.projections.SaleAmountBySizeView;
import com.estradahermanos.shoestock.repository.projections.SaleAmountByStockView;
import com.estradahermanos.shoestock.repository.projections.SaleAmountBySupplierView;
import com.estradahermanos.shoestock.repository.projections.SaleAmountByTypeView;
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
import static org.mockito.ArgumentMatchers.nullable;
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

    @Test
    void bottomProductsSkipsEmptyStockAndKeepsUnknownVariants()
    {
        ShoeStock empty = ShoeStock.builder().id(1).shoeId("OXF-001").color("Negro").size(40).stock(0).build();
        ShoeStock nameless = ShoeStock.builder().id(3).shoeId("NONE").color("Cafe").size(41).stock(2).build();
        ShoeStock named = ShoeStock.builder().id(4).shoeId("OXF-001").color("Negro").size(42).stock(3).build();
        when(saleRepository.sumAmountGroupedByStock(null, null)).thenReturn(List.of(
                stockAmount(1, 5L), stockAmount(2, 1L), stockAmount(4, 3L), stockAmount(3, 9L)));
        when(catalogLookup.variantsById(anyCollection())).thenReturn(Map.of(1, empty, 3, nameless, 4, named));
        when(catalogLookup.shoesByCode(anyCollection())).thenReturn(Map.of(
                "OXF-001", Shoe.builder().code("OXF-001").name("Oxford").supplier(1).type("Caballero").build()));
        when(saleRepository.findLastSaleDateGrouped()).thenReturn(List.of(lastSale(4, LocalDate.of(2026, 9, 1))));
        when(reportSalesMapper.toRank(any(ShoeStock.class), nullable(String.class), any(), any(), nullable(LocalDate.class)))
                .thenAnswer(invocation -> ProductSalesRankDTO.builder()
                        .shoeStockId(invocation.getArgument(0, ShoeStock.class).getId())
                        .build());

        List<ProductSalesRankDTO> inStock = service.bottomProducts(SalesReportFilterDTO.builder()
                .withStockOnly(true)
                .limit(10)
                .build());
        List<ProductSalesRankDTO> all = service.bottomProducts(SalesReportFilterDTO.builder().limit(10).build());

        assertEquals(2, inStock.size());
        assertEquals(4, all.size());
        assertEquals(2, all.get(0).getShoeStockId());
        assertEquals(0, all.get(0).getCurrentStock());
        assertEquals(4, all.get(1).getShoeStockId());
    }

    @Test
    void byPeriodMapsKnownAndMissingVariants()
    {
        Sale known = sale(2, LocalDate.of(2026, 9, 1));
        known.setShoeStockId(1);
        Sale missingShoe = sale(1, LocalDate.of(2026, 9, 1));
        missingShoe.setShoeStockId(2);
        Sale missingVariant = sale(3, LocalDate.of(2026, 9, 2));
        missingVariant.setShoeStockId(99);
        ShoeStock first = ShoeStock.builder().id(1).shoeId("OXF-001").color("Negro").size(42).stock(1).build();
        ShoeStock second = ShoeStock.builder().id(2).shoeId("GONE").color("Cafe").size(40).stock(1).build();
        when(saleRepository.findBySaleDateBetween(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 2)))
                .thenReturn(List.of(known, missingShoe, missingVariant));
        when(catalogLookup.variantsById(anyCollection())).thenReturn(Map.of(1, first, 2, second));
        when(catalogLookup.shoesByCode(anyCollection())).thenReturn(Map.of(
                "OXF-001", Shoe.builder().code("OXF-001").name("Oxford").build()));
        when(reportSalesMapper.toPeriodSale(any(Sale.class), nullable(String.class), nullable(String.class)))
                .thenReturn(PeriodSaleDTO.builder().amount(1).build());

        assertEquals(3, service.byPeriod(SalesReportFilterDTO.builder()
                .startDate(LocalDate.of(2026, 9, 1))
                .endDate(LocalDate.of(2026, 9, 2))
                .build()).size());
    }

    @Test
    void byCategoryCalculatesShare()
    {
        when(saleRepository.sumAmountGroupedByType(null, null)).thenReturn(List.of(
                typeAmount("Caballero", 25L, 2L),
                typeAmount("Dama", 75L, 3L)));

        List<SalesByCategoryDTO> result = service.byCategory(SalesReportFilterDTO.builder().build());

        assertEquals(25, result.get(0).getSharePercent());
        assertEquals(75, result.get(1).getSharePercent());
    }

    @Test
    void byCategoryReturnsZeroShareWhenTotalIsZero()
    {
        when(saleRepository.sumAmountGroupedByType(null, null)).thenReturn(List.of(typeAmount("Dama", 0L, 1L)));

        assertEquals(0, service.byCategory(SalesReportFilterDTO.builder().build()).get(0).getSharePercent());
    }

    @Test
    void bySizeAddsStockThatMatchesTypeAndCode()
    {
        when(shoeRepository.existsByCode("OXF-001")).thenReturn(true);
        when(saleRepository.sumAmountGroupedBySize(null, null, "Caballero", "OXF-001")).thenReturn(List.of(
                sizeAmount(42, "Caballero", 5L),
                sizeAmount(99, "Caballero", 1L)));
        when(catalogLookup.allVariants()).thenReturn(Map.of(
                1, variant(1, "MISSING", 40, 1),
                2, variant(2, "DAMA-1", 41, 2),
                3, variant(3, "OXF-002", 42, 3),
                4, variant(4, "OXF-001", 42, 4),
                5, variant(5, "OXF-001", 42, 1),
                6, variant(6, "OXF-001", 40, 2)));
        when(catalogLookup.allShoes()).thenReturn(Map.of(
                "DAMA-1", Shoe.builder().code("DAMA-1").name("Tacón").type("Dama").build(),
                "OXF-002", Shoe.builder().code("OXF-002").name("Bota").type("Caballero").build(),
                "OXF-001", Shoe.builder().code("OXF-001").name("Oxford").type("Caballero").build()));

        List<SalesBySizeDTO> result = service.bySize(SalesReportFilterDTO.builder()
                .type("Caballero")
                .shoeCode("OXF-001")
                .build());

        assertEquals(5, result.get(0).getCurrentStock());
        assertEquals(0, result.get(1).getCurrentStock());
    }

    @Test
    void bySupplierResolvesNamesAndShare()
    {
        when(saleRepository.sumAmountGroupedBySupplier(null, null)).thenReturn(List.of(
                supplierAmount(2, 40L, 1L),
                supplierAmount(9, 60L, 2L)));
        when(catalogLookup.supplierNames()).thenReturn(Map.of(2, "Maria Lopez"));

        List<SalesBySupplierDTO> result = service.bySupplier(SalesReportFilterDTO.builder().build());

        assertEquals("Maria Lopez", result.get(0).getSupplierName());
        assertEquals(40, result.get(0).getSharePercent());
        assertEquals(60, result.get(1).getSharePercent());
    }

    @Test
    void volumeGroupsByWeekAndMonth()
    {
        when(saleRepository.findBySaleDateBetween(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 10, 5)))
                .thenReturn(List.of(
                        sale(4, LocalDate.of(2026, 9, 1)),
                        sale(6, LocalDate.of(2026, 10, 5))));

        var byWeek = service.volume(SalesReportFilterDTO.builder()
                .startDate(LocalDate.of(2026, 9, 1))
                .endDate(LocalDate.of(2026, 10, 5))
                .groupBy("WEEK")
                .build());
        var byMonth = service.volume(SalesReportFilterDTO.builder()
                .startDate(LocalDate.of(2026, 9, 1))
                .endDate(LocalDate.of(2026, 10, 5))
                .groupBy("MONTH")
                .build());

        assertEquals(2, byWeek.size());
        assertEquals("2026-09", byMonth.get(0).getPeriod());
        assertEquals("2026-10", byMonth.get(1).getPeriod());
        assertEquals(4L, byMonth.get(0).getAmountSold());
    }

    @Test
    void wrapsUnexpectedFailures()
    {
        when(saleRepository.sumAmountGroupedByStock(any(), any())).thenThrow(new IllegalStateException("db"));
        assertFailed(() -> service.topProducts(SalesReportFilterDTO.builder().build()));
        assertFailed(() -> service.bottomProducts(SalesReportFilterDTO.builder().build()));

        when(saleRepository.findBySaleDateBetween(any(), any())).thenThrow(new IllegalStateException("db"));
        assertFailed(() -> service.byPeriod(datedFilter()));
        assertFailed(() -> service.volume(datedFilter()));

        when(saleRepository.sumAmountGroupedByType(any(), any())).thenThrow(new IllegalStateException("db"));
        assertFailed(() -> service.byCategory(SalesReportFilterDTO.builder().build()));

        when(saleRepository.sumAmountGroupedBySize(any(), any(), any(), any())).thenThrow(new IllegalStateException("db"));
        assertFailed(() -> service.bySize(SalesReportFilterDTO.builder().build()));

        when(saleRepository.sumAmountGroupedBySupplier(any(), any())).thenThrow(new IllegalStateException("db"));
        assertFailed(() -> service.bySupplier(SalesReportFilterDTO.builder().build()));
    }

    private void assertFailed(org.junit.jupiter.api.function.Executable executable)
    {
        BusinessException exception = assertThrows(BusinessException.class, executable);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getCode());
        assertEquals("Could not generate report", exception.getMessage());
    }

    private SalesReportFilterDTO datedFilter()
    {
        return SalesReportFilterDTO.builder()
                .startDate(LocalDate.of(2026, 9, 1))
                .endDate(LocalDate.of(2026, 9, 2))
                .groupBy("DAY")
                .build();
    }

    private ShoeStock variant(int id, String shoeId, int size, int stock)
    {
        return ShoeStock.builder().id(id).shoeId(shoeId).color("Negro").size(size).stock(stock).build();
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

    private Sale sale(int amount, LocalDate date)
    {
        return Sale.builder()
                .id(1)
                .shoeStockId(15)
                .amount(amount)
                .size(42)
                .saleDate(date)
                .build();
    }

    private LastSaleDateView lastSale(Integer id, LocalDate date)
    {
        return new LastSaleDateView()
        {
            @Override
            public Integer getShoeStockId()
            {
                return id;
            }

            @Override
            public LocalDate getLastSaleDate()
            {
                return date;
            }
        };
    }

    private SaleAmountByTypeView typeAmount(String type, Long amount, Long count)
    {
        return new SaleAmountByTypeView()
        {
            @Override
            public String getType()
            {
                return type;
            }

            @Override
            public Long getTotalAmount()
            {
                return amount;
            }

            @Override
            public Long getSaleCount()
            {
                return count;
            }
        };
    }

    private SaleAmountBySizeView sizeAmount(Integer size, String type, Long amount)
    {
        return new SaleAmountBySizeView()
        {
            @Override
            public Integer getSize()
            {
                return size;
            }

            @Override
            public String getType()
            {
                return type;
            }

            @Override
            public Long getTotalAmount()
            {
                return amount;
            }
        };
    }

    private SaleAmountBySupplierView supplierAmount(Integer supplierId, Long amount, Long styles)
    {
        return new SaleAmountBySupplierView()
        {
            @Override
            public Integer getSupplierId()
            {
                return supplierId;
            }

            @Override
            public Long getTotalAmount()
            {
                return amount;
            }

            @Override
            public Long getStylesSold()
            {
                return styles;
            }
        };
    }
}
