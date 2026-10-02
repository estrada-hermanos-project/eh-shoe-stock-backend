package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.request.SummaryReportFilterDTO;
import com.estradahermanos.shoestock.dto.response.BusinessSummaryDTO;
import com.estradahermanos.shoestock.dto.response.NewStyleDTO;
import com.estradahermanos.shoestock.dto.response.ProductSalesRankDTO;
import com.estradahermanos.shoestock.dto.response.SlowMoverDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.repository.entities.Order;
import com.estradahermanos.shoestock.repository.entities.OrderDetail;
import com.estradahermanos.shoestock.repository.entities.Sale;
import com.estradahermanos.shoestock.repository.entities.Shoe;
import com.estradahermanos.shoestock.repository.entities.ShoeStock;
import com.estradahermanos.shoestock.repository.projections.SaleAmountByStockView;
import com.estradahermanos.shoestock.repository.repositories.OrderDetailRepository;
import com.estradahermanos.shoestock.repository.repositories.OrderRepository;
import com.estradahermanos.shoestock.repository.repositories.SaleRepository;
import com.estradahermanos.shoestock.repository.repositories.ShoeRepository;
import com.estradahermanos.shoestock.utilities.OrderStatusEnum;
import com.estradahermanos.shoestock.utilities.ReportCatalogLookup;
import com.estradahermanos.shoestock.utilities.ReportValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportSummaryQueryServiceTest
{
    @Mock
    private ReportSalesQueryService reportSalesQueryService;

    @Mock
    private ReportInventoryQueryService reportInventoryQueryService;

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderDetailRepository orderDetailRepository;

    @Mock
    private ShoeRepository shoeRepository;

    @Mock
    private ReportCatalogLookup catalogLookup;

    private ReportSummaryQueryService service;

    @BeforeEach
    void setUp()
    {
        service = new ReportSummaryQueryService(
                reportSalesQueryService, reportInventoryQueryService, saleRepository, orderRepository,
                orderDetailRepository, shoeRepository, catalogLookup, new ReportValidator());
    }

    @Test
    void summaryUsesTheRequestedPeriodAndTopProduct()
    {
        stubSummary(List.of(ProductSalesRankDTO.builder().shoeName("Oxford").amountSold(8L).build()), false);

        BusinessSummaryDTO result = service.summary(SummaryReportFilterDTO.builder()
                .startDate(LocalDate.of(2026, 9, 1))
                .endDate(LocalDate.of(2026, 9, 30))
                .build());

        assertEquals(8L, result.getSales().getAmountSold());
        assertEquals(1L, result.getSales().getSaleCount());
        assertEquals("Oxford", result.getSales().getTopProduct().getShoeName());
        assertEquals(4L, result.getInventory().getPairsInStock());
        assertEquals(1L, result.getInventory().getLowStockCount());
        assertEquals(1L, result.getInventory().getOutOfStockCount());
        assertEquals(1L, result.getInventory().getSlowMoverCount());
        assertEquals(1L, result.getMerchandise().getPendingOrders());
        assertEquals(3L, result.getMerchandise().getPairsPending());
        assertEquals(5L, result.getMerchandise().getPairsReceived());
        assertEquals(2L, result.getCatalog().getStyleCount());
        assertEquals(1L, result.getCatalog().getNewStyles());
    }

    @Test
    void summaryDefaultsToTheCurrentMonthAndAllowsAnEmptyTopProduct()
    {
        stubSummary(List.of(), true);

        BusinessSummaryDTO result = service.summary(SummaryReportFilterDTO.builder().build());

        assertNull(result.getSales().getTopProduct());
        assertEquals(0L, result.getMerchandise().getPairsPending());
    }

    @Test
    void summaryRethrowsBusinessExceptions()
    {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.summary(SummaryReportFilterDTO.builder()
                        .startDate(LocalDate.of(2026, 9, 1))
                        .build()));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
    }

    @Test
    void newStylesFiltersTypeAndSupplier()
    {
        when(catalogLookup.supplierNames()).thenReturn(Map.of(2, "Maria Lopez"));
        when(shoeRepository.findByCreatedAtBetween(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)))
                .thenReturn(List.of(
                        shoe("OXF-001", "Oxford", "Caballero", 2),
                        shoe("DAMA-1", "Tacon", "Dama", 2),
                        shoe("BOT-1", "Bota", "Caballero", 9)));

        List<NewStyleDTO> filtered = service.newStyles(SummaryReportFilterDTO.builder()
                .startDate(LocalDate.of(2026, 9, 1))
                .endDate(LocalDate.of(2026, 9, 30))
                .type("Caballero")
                .supplierId(2)
                .build());
        List<NewStyleDTO> all = service.newStyles(SummaryReportFilterDTO.builder()
                .startDate(LocalDate.of(2026, 9, 1))
                .endDate(LocalDate.of(2026, 9, 30))
                .build());

        assertEquals(1, filtered.size());
        assertEquals("Oxford", filtered.get(0).getName());
        assertEquals("Maria Lopez", filtered.get(0).getSupplierName());
        assertEquals(3, all.size());
    }

    @Test
    void wrapsUnexpectedFailures()
    {
        when(saleRepository.sumAmountGroupedByStock(any(), any())).thenThrow(new IllegalStateException("db"));
        BusinessException summary = assertThrows(BusinessException.class,
                () -> service.summary(SummaryReportFilterDTO.builder()
                        .startDate(LocalDate.of(2026, 9, 1))
                        .endDate(LocalDate.of(2026, 9, 30))
                        .build()));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, summary.getCode());

        when(shoeRepository.findByCreatedAtBetween(any(), any())).thenThrow(new IllegalStateException("db"));
        BusinessException styles = assertThrows(BusinessException.class,
                () -> service.newStyles(SummaryReportFilterDTO.builder()
                        .startDate(LocalDate.of(2026, 9, 1))
                        .endDate(LocalDate.of(2026, 9, 30))
                        .build()));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, styles.getCode());
    }

    private void stubSummary(List<ProductSalesRankDTO> top, boolean emptyOrders)
    {
        when(saleRepository.sumAmountGroupedByStock(any(), any())).thenReturn(List.of(stockAmount(15, 8L)));
        when(saleRepository.findBySaleDateBetween(any(), any())).thenReturn(List.of(
                Sale.builder().id(1).shoeStockId(15).amount(8).size(42).saleDate(LocalDate.of(2026, 9, 2)).build()));
        when(reportSalesQueryService.topProducts(any())).thenReturn(top);
        when(catalogLookup.allVariants()).thenReturn(Map.of(
                1, ShoeStock.builder().id(1).shoeId("OXF-001").stock(0).minStock(1).size(42).color("Negro").build(),
                2, ShoeStock.builder().id(2).shoeId("OXF-001").stock(4).minStock(1).size(40).color("Cafe").build()));
        when(reportInventoryQueryService.slowMovers(any())).thenReturn(List.of(SlowMoverDTO.builder().build()));
        when(orderRepository.findByStatus(OrderStatusEnum.PENDIENTE)).thenReturn(emptyOrders
                ? List.of()
                : List.of(Order.builder().id("ORDEN-1").status(OrderStatusEnum.PENDIENTE).build()));
        when(orderRepository.findByStatusAndDateRange(any(), any(), any())).thenReturn(emptyOrders
                ? List.of()
                : List.of(Order.builder().id("ORDEN-2").status(OrderStatusEnum.RECIBIDA).build()));
        if (!emptyOrders)
        {
            List<OrderDetail> details = List.of(
                    OrderDetail.builder().orderId("ORDEN-1").amount(3).build(),
                    OrderDetail.builder().orderId("ORDEN-2").amount(5).build());
            when(orderDetailRepository.findByOrderIdIn(anyCollection())).thenAnswer(invocation ->
            {
                Collection<String> ids = invocation.getArgument(0);
                return details.stream().filter(detail -> ids.contains(detail.getOrderId())).toList();
            });
        }
        when(shoeRepository.findAll()).thenReturn(List.of(
                shoe("OXF-001", "Oxford", "Caballero", 2),
                shoe("DAMA-1", "Tacon", "Dama", 2)));
        when(shoeRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of(
                shoe("OXF-001", "Oxford", "Caballero", 2)));
    }

    private Shoe shoe(String code, String name, String type, int supplier)
    {
        return Shoe.builder()
                .code(code)
                .name(name)
                .type(type)
                .supplier(supplier)
                .description("Clasico")
                .createdAt(LocalDate.of(2026, 9, 4))
                .build();
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
