package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.request.InventoryReportFilterDTO;
import com.estradahermanos.shoestock.dto.response.InventoryGroupDTO;
import com.estradahermanos.shoestock.dto.response.InventoryStatusDTO;
import com.estradahermanos.shoestock.dto.response.LowStockDTO;
import com.estradahermanos.shoestock.dto.response.OutOfStockDTO;
import com.estradahermanos.shoestock.dto.response.RestockSuggestionDTO;
import com.estradahermanos.shoestock.dto.response.SlowMoverDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.mapper.ReportInventoryMapper;
import com.estradahermanos.shoestock.repository.entities.Order;
import com.estradahermanos.shoestock.repository.entities.OrderDetail;
import com.estradahermanos.shoestock.repository.entities.Shoe;
import com.estradahermanos.shoestock.repository.entities.ShoeStock;
import com.estradahermanos.shoestock.repository.projections.LastSaleDateView;
import com.estradahermanos.shoestock.repository.projections.SaleAmountByStockView;
import com.estradahermanos.shoestock.utilities.OrderStatusEnum;
import com.estradahermanos.shoestock.repository.repositories.OrderDetailRepository;
import com.estradahermanos.shoestock.repository.repositories.OrderRepository;
import com.estradahermanos.shoestock.repository.repositories.SaleRepository;
import com.estradahermanos.shoestock.repository.repositories.ShoeStockRepository;
import com.estradahermanos.shoestock.utilities.ReportCatalogLookup;
import com.estradahermanos.shoestock.utilities.ReportValidator;
import com.estradahermanos.shoestock.utilities.RestockAdviceEnum;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportInventoryQueryServiceTest
{
    @Mock
    private ShoeStockRepository shoeStockRepository;

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderDetailRepository orderDetailRepository;

    @Mock
    private ReportCatalogLookup catalogLookup;

    @Mock
    private ReportInventoryMapper reportInventoryMapper;

    private ReportInventoryQueryService service;

    @BeforeEach
    void setUp()
    {
        service = new ReportInventoryQueryService(
                shoeStockRepository, saleRepository, orderRepository, orderDetailRepository,
                catalogLookup, new ReportValidator(), reportInventoryMapper);
    }

    @Test
    void slowMoversIncludesNeverSoldVariantsWithStock()
    {
        ShoeStock variant = ShoeStock.builder()
                .id(15).shoeId("OXF-001").color("Negro").size(42).stock(4).minStock(1).build();
        when(catalogLookup.allVariants()).thenReturn(Map.of(15, variant));
        when(catalogLookup.allShoes()).thenReturn(Map.of("OXF-001",
                Shoe.builder().code("OXF-001").name("Oxford").supplier(2).type("Caballero").build()));
        when(catalogLookup.supplierNames()).thenReturn(Map.of(2, "Maria Lopez"));
        when(saleRepository.findLastSaleDateGrouped()).thenReturn(List.of());

        List<SlowMoverDTO> result = service.slowMovers(InventoryReportFilterDTO.builder().days(45).build());

        assertEquals(1, result.size());
        assertEquals(15, result.get(0).getShoeStockId());
        assertNull(result.get(0).getLastSaleDate());
        assertEquals("Maria Lopez", result.get(0).getSupplierName());
    }

    @Test
    void restockMarksZeroStockAsReplenish()
    {
        ShoeStock variant = ShoeStock.builder()
                .id(15).shoeId("OXF-001").color("Negro").size(42).stock(0).minStock(2).build();
        when(catalogLookup.allVariants()).thenReturn(Map.of(15, variant));
        when(catalogLookup.allShoes()).thenReturn(Map.of("OXF-001",
                Shoe.builder().code("OXF-001").name("Oxford").supplier(2).type("Caballero").build()));
        when(saleRepository.sumAmountGroupedByStock(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of());

        List<RestockSuggestionDTO> result = service.restock(InventoryReportFilterDTO.builder().build());

        assertEquals(1, result.size());
        assertEquals(RestockAdviceEnum.REPONER, result.get(0).getAdvice());
    }

    @Test
    void statusFiltersTypeSupplierAndOutOfStock()
    {
        ShoeStock kept = variant(1, "OXF-001", 4, 2);
        ShoeStock zero = variant(2, "OXF-001", 0, 1);
        ShoeStock otherType = variant(3, "DAMA-1", 3, 1);
        ShoeStock otherSupplier = variant(4, "BOT-1", 5, 1);
        ShoeStock nameless = variant(5, "GONE", 1, 1);
        when(catalogLookup.allVariants()).thenReturn(Map.of(1, kept, 2, zero, 3, otherType, 4, otherSupplier, 5, nameless));
        when(catalogLookup.allShoes()).thenReturn(Map.of(
                "OXF-001", shoe("OXF-001", "Oxford", "Caballero", 2),
                "DAMA-1", shoe("DAMA-1", "Tacon", "Dama", 2),
                "BOT-1", shoe("BOT-1", "Bota", "Caballero", 9)));
        when(reportInventoryMapper.toStatus(any(ShoeStock.class), nullable(String.class)))
                .thenAnswer(invocation -> InventoryStatusDTO.builder()
                        .shoeStockId(invocation.getArgument(0, ShoeStock.class).getId())
                        .shoeName(invocation.getArgument(1, String.class))
                        .build());

        List<InventoryStatusDTO> included = service.status(InventoryReportFilterDTO.builder()
                .type("Caballero")
                .supplierId(2)
                .build());
        List<InventoryStatusDTO> inStock = service.status(InventoryReportFilterDTO.builder()
                .type("Caballero")
                .supplierId(2)
                .includeOutOfStock(false)
                .build());
        List<InventoryStatusDTO> all = service.status(InventoryReportFilterDTO.builder().build());

        assertEquals(2, included.size());
        assertEquals(1, inStock.size());
        assertEquals(5, all.size());
        assertTrue(all.stream().anyMatch(row -> row.getShoeName() == null));
    }

    @Test
    void slowMoversSkipsRecentSalesAndEmptyStock()
    {
        ShoeStock empty = variant(1, "OXF-001", 0, 1);
        ShoeStock recent = variant(2, "OXF-001", 4, 1);
        ShoeStock old = variant(3, "OXF-001", 4, 1);
        when(catalogLookup.allVariants()).thenReturn(Map.of(1, empty, 2, recent, 3, old));
        when(catalogLookup.allShoes()).thenReturn(Map.of("OXF-001", shoe("OXF-001", "Oxford", "Caballero", 2)));
        when(catalogLookup.supplierNames()).thenReturn(Map.of(2, "Maria Lopez"));
        when(saleRepository.findLastSaleDateGrouped()).thenReturn(List.of(
                lastSale(2, LocalDate.now()),
                lastSale(3, LocalDate.now().minusDays(90))));

        List<SlowMoverDTO> result = service.slowMovers(InventoryReportFilterDTO.builder().days(45).build());

        assertEquals(1, result.size());
        assertEquals(3, result.get(0).getShoeStockId());
    }

    @Test
    void lowStockKeepsVariantsInsideTheFilter()
    {
        ShoeStock kept = variant(1, "OXF-001", 1, 4);
        ShoeStock excluded = variant(2, "DAMA-1", 0, 2);
        when(catalogLookup.allVariants()).thenReturn(Map.of(1, kept, 2, excluded));
        when(catalogLookup.allShoes()).thenReturn(Map.of("OXF-001", shoe("OXF-001", "Oxford", "Caballero", 2)));
        when(catalogLookup.supplierNames()).thenReturn(Map.of());
        when(shoeStockRepository.findLowStock()).thenReturn(List.of(kept, excluded));
        when(reportInventoryMapper.toLowStock(any(), nullable(String.class), any(), nullable(String.class)))
                .thenAnswer(invocation -> LowStockDTO.builder()
                        .shoeStockId(invocation.getArgument(0, ShoeStock.class).getId())
                        .pairsBelowMin(invocation.getArgument(2, Integer.class))
                        .build());

        List<LowStockDTO> result = service.lowStock(InventoryReportFilterDTO.builder().type("Caballero").build());

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getShoeStockId());
        assertEquals(3, result.get(0).getPairsBelowMin());
    }

    @Test
    void outOfStockMarksPendingOrders()
    {
        ShoeStock zero = variant(1, "OXF-001", 0, 1);
        ShoeStock nameless = variant(2, "GONE", 0, 1);
        when(shoeStockRepository.findByStock(0)).thenReturn(List.of(zero, nameless));
        when(catalogLookup.allShoes()).thenReturn(Map.of("OXF-001", shoe("OXF-001", "Oxford", "Caballero", 2)));
        when(catalogLookup.supplierNames()).thenReturn(Map.of(2, "Maria Lopez"));
        when(saleRepository.sumAmountGroupedByStock(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)))
                .thenReturn(List.of(stockAmount(1, 6L)));
        when(orderRepository.findByStatus(OrderStatusEnum.PENDIENTE)).thenReturn(List.of(
                Order.builder().id("ORDEN-1").status(OrderStatusEnum.PENDIENTE).build()));
        when(orderDetailRepository.findByOrderIdIn(any())).thenReturn(List.of(
                OrderDetail.builder().orderId("ORDEN-1").shoeStockId(1).amount(2).build()));

        List<OutOfStockDTO> result = service.outOfStock(InventoryReportFilterDTO.builder()
                .startDate(LocalDate.of(2026, 9, 1))
                .endDate(LocalDate.of(2026, 9, 30))
                .build());

        assertEquals(6L, result.get(0).getAmountSold());
        assertTrue(result.get(0).getHasPendingOrder());
        assertFalse(result.get(1).getHasPendingOrder());
        assertNull(result.get(1).getShoeName());
    }

    @Test
    void outOfStockWithoutPendingOrders()
    {
        when(shoeStockRepository.findByStock(0)).thenReturn(List.of(variant(1, "OXF-001", 0, 1)));
        when(catalogLookup.allShoes()).thenReturn(Map.of());
        when(catalogLookup.supplierNames()).thenReturn(Map.of());
        when(saleRepository.sumAmountGroupedByStock(null, null)).thenReturn(List.of());
        when(orderRepository.findByStatus(OrderStatusEnum.PENDIENTE)).thenReturn(List.of());

        assertFalse(service.outOfStock(InventoryReportFilterDTO.builder().build()).get(0).getHasPendingOrder());
    }

    @Test
    void restockSortsAdviceAndUsesExplicitPeriod()
    {
        ShoeStock replenish = variant(1, "OXF-001", 0, 2);
        ShoeStock watch = variant(2, "OXF-001", 2, 1);
        ShoeStock skip = variant(3, "OXF-001", 8, 1);
        ShoeStock low = variant(4, "GONE", 1, 3);
        when(catalogLookup.allVariants()).thenReturn(Map.of(1, replenish, 2, watch, 3, skip, 4, low));
        when(catalogLookup.allShoes()).thenReturn(Map.of("OXF-001", shoe("OXF-001", "Oxford", "Caballero", 2)));
        when(saleRepository.sumAmountGroupedByStock(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)))
                .thenReturn(List.of(stockAmount(2, 9L), stockAmount(3, 1L)));

        List<RestockSuggestionDTO> result = service.restock(InventoryReportFilterDTO.builder()
                .startDate(LocalDate.of(2026, 9, 1))
                .endDate(LocalDate.of(2026, 9, 30))
                .build());

        assertEquals(RestockAdviceEnum.REPONER, result.get(0).getAdvice());
        assertEquals(RestockAdviceEnum.VIGILAR, result.get(2).getAdvice());
        assertEquals(RestockAdviceEnum.NO_PEDIR, result.get(3).getAdvice());
        assertTrue(result.stream().anyMatch(row -> row.getShoeName() == null));
    }

    @Test
    void byGroupBucketsTypeAndSupplier()
    {
        ShoeStock first = variant(1, "OXF-001", 0, 2);
        ShoeStock second = variant(2, "OXF-001", 5, 1);
        ShoeStock unknownSupplier = variant(3, "BOT-1", 1, 1);
        ShoeStock nameless = variant(4, "GONE", 2, 1);
        when(catalogLookup.allVariants()).thenReturn(Map.of(1, first, 2, second, 3, unknownSupplier, 4, nameless));
        when(catalogLookup.allShoes()).thenReturn(Map.of(
                "OXF-001", shoe("OXF-001", "Oxford", "Caballero", 2),
                "BOT-1", shoe("BOT-1", "Bota", "Caballero", 8)));
        when(catalogLookup.supplierNames()).thenReturn(Map.of(2, "Maria Lopez"));

        List<InventoryGroupDTO> byType = service.byGroup(InventoryReportFilterDTO.builder().groupBy("TYPE").build());
        List<InventoryGroupDTO> bySupplier = service.byGroup(InventoryReportFilterDTO.builder().groupBy("SUPPLIER").build());

        InventoryGroupDTO caballero = byType.stream()
                .filter(row -> "Caballero".equals(row.getGroup()))
                .findFirst()
                .orElseThrow();
        assertEquals(3L, caballero.getVariantCount());
        assertEquals(1L, caballero.getOutOfStockCount());
        assertEquals(2L, caballero.getLowStockCount());
        assertTrue(byType.stream().anyMatch(row -> "Unknown".equals(row.getGroup())));
        assertTrue(bySupplier.stream().anyMatch(row -> "Maria Lopez".equals(row.getGroup())));
        assertTrue(bySupplier.stream().anyMatch(row -> "Unknown".equals(row.getGroup())));
    }

    @Test
    void wrapsUnexpectedFailures()
    {
        when(catalogLookup.allVariants()).thenThrow(new IllegalStateException("db"));
        assertFailed(() -> service.status(InventoryReportFilterDTO.builder().build()));
        assertFailed(() -> service.slowMovers(InventoryReportFilterDTO.builder().build()));
        assertFailed(() -> service.restock(InventoryReportFilterDTO.builder().build()));
        assertFailed(() -> service.byGroup(InventoryReportFilterDTO.builder().groupBy("TYPE").build()));

        doReturn(Map.of()).when(catalogLookup).allVariants();
        when(shoeStockRepository.findLowStock()).thenThrow(new IllegalStateException("db"));
        assertFailed(() -> service.lowStock(InventoryReportFilterDTO.builder().build()));

        when(saleRepository.sumAmountGroupedByStock(any(), any())).thenThrow(new IllegalStateException("db"));
        assertFailed(() -> service.outOfStock(InventoryReportFilterDTO.builder().build()));
    }

    private void assertFailed(org.junit.jupiter.api.function.Executable executable)
    {
        BusinessException exception = assertThrows(BusinessException.class, executable);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getCode());
    }

    private ShoeStock variant(int id, String shoeId, int stock, int minStock)
    {
        return ShoeStock.builder()
                .id(id)
                .shoeId(shoeId)
                .color("Negro")
                .size(42)
                .stock(stock)
                .minStock(minStock)
                .build();
    }

    private Shoe shoe(String code, String name, String type, int supplier)
    {
        return Shoe.builder().code(code).name(name).type(type).supplier(supplier).build();
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
}
