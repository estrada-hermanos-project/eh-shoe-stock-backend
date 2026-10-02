package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.request.MerchandiseReportFilterDTO;
import com.estradahermanos.shoestock.dto.response.OrderDetailResponseDTO;
import com.estradahermanos.shoestock.dto.response.OrderedNotSellingDTO;
import com.estradahermanos.shoestock.dto.response.OrderedVsSoldDTO;
import com.estradahermanos.shoestock.dto.response.OrdersBySupplierDTO;
import com.estradahermanos.shoestock.dto.response.PendingOrderReportDTO;
import com.estradahermanos.shoestock.dto.response.ReceivedOrderReportDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.mapper.OrderDetailMapper;
import com.estradahermanos.shoestock.mapper.ReportMerchandiseMapper;
import com.estradahermanos.shoestock.repository.entities.Order;
import com.estradahermanos.shoestock.repository.entities.OrderDetail;
import com.estradahermanos.shoestock.repository.entities.Shoe;
import com.estradahermanos.shoestock.repository.entities.ShoeStock;
import com.estradahermanos.shoestock.repository.entities.Supplier;
import com.estradahermanos.shoestock.repository.projections.LastSaleDateView;
import com.estradahermanos.shoestock.repository.projections.SaleAmountByStockView;
import com.estradahermanos.shoestock.repository.repositories.OrderDetailRepository;
import com.estradahermanos.shoestock.repository.repositories.OrderRepository;
import com.estradahermanos.shoestock.repository.repositories.SaleRepository;
import com.estradahermanos.shoestock.repository.repositories.SupplierRepository;
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
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportMerchandiseQueryServiceTest
{
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderDetailRepository orderDetailRepository;

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ReportCatalogLookup catalogLookup;

    @Mock
    private ReportMerchandiseMapper reportMerchandiseMapper;

    @Mock
    private OrderDetailMapper orderDetailMapper;

    private ReportMerchandiseQueryService service;

    @BeforeEach
    void setUp()
    {
        service = new ReportMerchandiseQueryService(
                orderRepository, orderDetailRepository, saleRepository, supplierRepository,
                catalogLookup, new ReportValidator(), reportMerchandiseMapper, orderDetailMapper);
    }

    @Test
    void pendingAssemblesOrderWithDetails()
    {
        Order order = Order.builder()
                .id("ORDEN-101")
                .supplier(2)
                .orderDeliveryDate(LocalDate.now().minusDays(3))
                .status(OrderStatusEnum.PENDIENTE)
                .build();
        OrderDetail detail = OrderDetail.builder().id(1).orderId("ORDEN-101").shoeStockId(15).amount(4).build();
        ShoeStock variant = ShoeStock.builder().id(15).shoeId("OXF-001").color("Negro").size(42).stock(2).build();

        when(orderRepository.findByStatus(OrderStatusEnum.PENDIENTE)).thenReturn(List.of(order));
        when(orderDetailRepository.findByOrderIdIn(anyCollection())).thenReturn(List.of(detail));
        when(catalogLookup.supplierNames()).thenReturn(Map.of(2, "Maria Lopez"));
        when(catalogLookup.variantsById(anyCollection())).thenReturn(Map.of(15, variant));
        when(catalogLookup.shoesByCode(anyCollection())).thenReturn(Map.of(
                "OXF-001", Shoe.builder().code("OXF-001").name("Oxford").supplier(2).build()));
        when(orderDetailMapper.toResponse(eq(detail), eq("Oxford")))
                .thenReturn(OrderDetailResponseDTO.builder().shoeStockId(15).shoeName("Oxford").amount(4).build());
        when(reportMerchandiseMapper.toPending(eq(order), eq("Maria Lopez"), anyLong(), eq(4), anyList()))
                .thenReturn(PendingOrderReportDTO.builder()
                        .orderId("ORDEN-101")
                        .supplierName("Maria Lopez")
                        .totalPairs(4)
                        .daysPending(3L)
                        .build());

        List<PendingOrderReportDTO> result = service.pending(MerchandiseReportFilterDTO.builder().build());

        assertEquals(1, result.size());
        assertEquals("ORDEN-101", result.get(0).getOrderId());
        assertEquals(4, result.get(0).getTotalPairs());
    }

    @Test
    void pendingFiltersBySupplier()
    {
        Order order = order("ORDEN-2", 2, LocalDate.now(), OrderStatusEnum.PENDIENTE);
        when(orderRepository.findByStatusAndSupplier(OrderStatusEnum.PENDIENTE, 2)).thenReturn(List.of(order));
        when(orderDetailRepository.findByOrderIdIn(anyCollection())).thenReturn(List.of());
        when(catalogLookup.supplierNames()).thenReturn(Map.of(2, "Maria Lopez"));
        when(catalogLookup.variantsById(anyCollection())).thenReturn(Map.of());
        when(catalogLookup.shoesByCode(anyCollection())).thenReturn(Map.of());
        when(reportMerchandiseMapper.toPending(eq(order), eq("Maria Lopez"), anyLong(), eq(0), anyList()))
                .thenReturn(PendingOrderReportDTO.builder().orderId("ORDEN-2").totalPairs(0).build());

        assertEquals("ORDEN-2", service.pending(MerchandiseReportFilterDTO.builder().supplierId(2).build())
                .get(0).getOrderId());
    }

    @Test
    void receivedMapsOrdersWithAndWithoutSupplier()
    {
        Order order = order("ORDEN-3", 2, LocalDate.of(2026, 9, 2), OrderStatusEnum.RECIBIDA);
        OrderDetail known = OrderDetail.builder().id(1).orderId("ORDEN-3").shoeStockId(15).amount(4).build();
        OrderDetail unknown = OrderDetail.builder().id(2).orderId("ORDEN-3").shoeStockId(99).amount(1).build();
        ShoeStock variant = ShoeStock.builder().id(15).shoeId("OXF-001").color("Negro").size(42).stock(1).build();
        when(orderRepository.findByStatusAndDateRange(
                OrderStatusEnum.RECIBIDA, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)))
                .thenReturn(List.of(order));
        when(orderRepository.findByStatusAndSupplierAndDateRange(
                eq(OrderStatusEnum.RECIBIDA), eq(2), eq(LocalDate.of(2026, 9, 1)), eq(LocalDate.of(2026, 9, 30))))
                .thenReturn(List.of(order));
        when(orderDetailRepository.findByOrderIdIn(anyCollection())).thenReturn(List.of(known, unknown));
        when(catalogLookup.supplierNames()).thenReturn(Map.of(2, "Maria Lopez"));
        when(catalogLookup.variantsById(anyCollection())).thenReturn(Map.of(15, variant));
        when(catalogLookup.shoesByCode(anyCollection())).thenReturn(Map.of(
                "OXF-001", Shoe.builder().code("OXF-001").name("Oxford").supplier(2).build()));
        when(orderDetailMapper.toResponse(any(OrderDetail.class), nullable(String.class)))
                .thenAnswer(invocation -> OrderDetailResponseDTO.builder()
                        .shoeName(invocation.getArgument(1, String.class))
                        .amount(invocation.getArgument(0, OrderDetail.class).getAmount())
                        .build());
        when(reportMerchandiseMapper.toReceived(eq(order), eq("Maria Lopez"), eq(5), anyList()))
                .thenReturn(ReceivedOrderReportDTO.builder().orderId("ORDEN-3").totalPairs(5).build());

        MerchandiseReportFilterDTO filter = MerchandiseReportFilterDTO.builder()
                .startDate(LocalDate.of(2026, 9, 1))
                .endDate(LocalDate.of(2026, 9, 30))
                .build();
        assertEquals(5, service.received(filter).get(0).getTotalPairs());
        assertEquals(5, service.received(MerchandiseReportFilterDTO.builder()
                .startDate(LocalDate.of(2026, 9, 1))
                .endDate(LocalDate.of(2026, 9, 30))
                .supplierId(2)
                .build()).get(0).getTotalPairs());
    }

    @Test
    void bySupplierCountsPendingAndReceived()
    {
        Supplier maria = Supplier.builder().id(2).fullName("Maria Lopez").phone("555").build();
        when(supplierRepository.findAll()).thenReturn(List.of(maria));
        when(orderRepository.findAll()).thenReturn(List.of());
        assertEquals(1, service.bySupplier(MerchandiseReportFilterDTO.builder().build()).size());

        Order pending = order("ORDEN-4", 2, LocalDate.of(2026, 9, 2), OrderStatusEnum.PENDIENTE);
        Order received = order("ORDEN-5", 2, LocalDate.of(2026, 9, 3), OrderStatusEnum.RECIBIDA);
        Order unknown = order("ORDEN-6", 9, LocalDate.of(2026, 9, 4), OrderStatusEnum.PENDIENTE);
        when(orderRepository.findAll()).thenReturn(List.of(pending, received, unknown));
        when(orderRepository.findByOrderDeliveryDateBetween(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)))
                .thenReturn(List.of(pending, received));
        when(orderDetailRepository.findByOrderIdIn(anyCollection())).thenReturn(List.of(
                OrderDetail.builder().orderId("ORDEN-4").shoeStockId(15).amount(4).build(),
                OrderDetail.builder().orderId("ORDEN-5").shoeStockId(15).amount(6).build()));

        List<OrdersBySupplierDTO> all = service.bySupplier(MerchandiseReportFilterDTO.builder().build());
        List<OrdersBySupplierDTO> ranged = service.bySupplier(MerchandiseReportFilterDTO.builder()
                .startDate(LocalDate.of(2026, 9, 1))
                .endDate(LocalDate.of(2026, 9, 30))
                .build());

        assertEquals(1L, all.get(0).getPendingCount());
        assertEquals(1L, all.get(0).getReceivedCount());
        assertEquals(10L, all.get(0).getPairsRequested());
        assertEquals(6L, all.get(0).getPairsReceived());
        assertEquals(1L, ranged.get(0).getPendingCount());
    }

    @Test
    void orderedVsSoldKeepsEveryVariantWhenSupplierIsMissing()
    {
        Order order = order("ORDEN-7", 2, LocalDate.of(2026, 9, 2), OrderStatusEnum.RECIBIDA);
        when(orderRepository.findByOrderDeliveryDateBetween(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)))
                .thenReturn(List.of(order));
        when(orderDetailRepository.findByOrderIdIn(anyCollection())).thenReturn(List.of(
                OrderDetail.builder().orderId("ORDEN-7").shoeStockId(15).amount(4).build(),
                OrderDetail.builder().orderId("ORDEN-7").shoeStockId(99).amount(1).build()));
        when(saleRepository.sumAmountGroupedByStock(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)))
                .thenReturn(List.of(stockAmount(15, 2L)));
        when(catalogLookup.allVariants()).thenReturn(Map.of(15,
                ShoeStock.builder().id(15).shoeId("OXF-001").color("Negro").size(42).stock(3).build()));
        when(catalogLookup.allShoes()).thenReturn(Map.of(
                "OXF-001", Shoe.builder().code("OXF-001").name("Oxford").supplier(2).build()));

        List<OrderedVsSoldDTO> result = service.orderedVsSold(MerchandiseReportFilterDTO.builder()
                .startDate(LocalDate.of(2026, 9, 1))
                .endDate(LocalDate.of(2026, 9, 30))
                .build());

        assertEquals(2, result.size());
    }

    @Test
    void orderedVsSoldFiltersBySupplierAndDate()
    {
        Order inside = order("ORDEN-8", 2, LocalDate.of(2026, 9, 10), OrderStatusEnum.RECIBIDA);
        Order outside = order("ORDEN-9", 2, LocalDate.of(2026, 8, 1), OrderStatusEnum.RECIBIDA);
        when(orderRepository.findBySupplier(2)).thenReturn(List.of(inside, outside));
        when(orderDetailRepository.findByOrderIdIn(anyCollection())).thenReturn(List.of(
                OrderDetail.builder().orderId("ORDEN-8").shoeStockId(15).amount(4).build(),
                OrderDetail.builder().orderId("ORDEN-8").shoeStockId(16).amount(2).build(),
                OrderDetail.builder().orderId("ORDEN-8").shoeStockId(17).amount(1).build(),
                OrderDetail.builder().orderId("ORDEN-8").shoeStockId(99).amount(1).build()));
        when(saleRepository.sumAmountGroupedByStock(any(), any())).thenReturn(List.of());
        ShoeStock match = ShoeStock.builder().id(15).shoeId("OXF-001").color("Negro").size(42).stock(3).build();
        ShoeStock other = ShoeStock.builder().id(16).shoeId("DAMA-1").color("Rojo").size(37).stock(1).build();
        ShoeStock nameless = ShoeStock.builder().id(17).shoeId("GONE").color("Cafe").size(40).stock(1).build();
        when(catalogLookup.allVariants()).thenReturn(Map.of(15, match, 16, other, 17, nameless));
        when(catalogLookup.allShoes()).thenReturn(Map.of(
                "OXF-001", Shoe.builder().code("OXF-001").name("Oxford").supplier(2).build(),
                "DAMA-1", Shoe.builder().code("DAMA-1").name("Tacon").supplier(9).build()));

        List<OrderedVsSoldDTO> result = service.orderedVsSold(MerchandiseReportFilterDTO.builder()
                .startDate(LocalDate.of(2026, 9, 1))
                .endDate(LocalDate.of(2026, 9, 30))
                .supplierId(2)
                .build());

        assertEquals(1, result.size());
        assertEquals(15, result.get(0).getShoeStockId());
        assertEquals(4L, result.get(0).getPairsOrdered());
        assertEquals("Oxford", result.get(0).getShoeName());
    }

    @Test
    void orderedNotSellingSkipsActiveVariants()
    {
        when(orderRepository.findAll()).thenReturn(List.of(
                order("ORDEN-10", 2, LocalDate.of(2026, 9, 2), OrderStatusEnum.RECIBIDA)));
        when(orderRepository.findByOrderDeliveryDateBetween(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)))
                .thenReturn(List.of());
        when(orderDetailRepository.findByOrderIdIn(anyCollection())).thenReturn(List.of(
                OrderDetail.builder().orderId("ORDEN-10").shoeStockId(1).amount(0).build(),
                OrderDetail.builder().orderId("ORDEN-10").shoeStockId(2).amount(3).build(),
                OrderDetail.builder().orderId("ORDEN-10").shoeStockId(3).amount(2).build(),
                OrderDetail.builder().orderId("ORDEN-10").shoeStockId(4).amount(2).build(),
                OrderDetail.builder().orderId("ORDEN-10").shoeStockId(5).amount(2).build()));
        when(saleRepository.sumAmountGroupedByStock(any(), any())).thenReturn(List.of(
                stockAmount(3, 4L), stockAmount(4, 1L)));
        when(saleRepository.findLastSaleDateGrouped()).thenReturn(List.of(
                lastSale(3, LocalDate.now()),
                lastSale(4, LocalDate.now().minusDays(100))));
        when(catalogLookup.allVariants()).thenReturn(Map.of(
                2, ShoeStock.builder().id(2).shoeId("OXF-001").color("Negro").size(42).stock(2).build(),
                4, ShoeStock.builder().id(4).shoeId("OXF-001").color("Cafe").size(40).stock(1).build()));
        when(catalogLookup.allShoes()).thenReturn(Map.of(
                "OXF-001", Shoe.builder().code("OXF-001").name("Oxford").supplier(2).build()));
        when(catalogLookup.supplierNames()).thenReturn(Map.of(2, "Maria Lopez"));

        List<OrderedNotSellingDTO> result = service.orderedNotSelling(MerchandiseReportFilterDTO.builder().build());
        assertEquals(0, service.orderedNotSelling(MerchandiseReportFilterDTO.builder()
                .startDate(LocalDate.of(2026, 9, 1))
                .endDate(LocalDate.of(2026, 9, 30))
                .build()).size());

        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(row -> row.getShoeStockId() == 5 && row.getShoeName() == null));
        assertTrue(result.stream().anyMatch(row -> row.getShoeStockId() == 2 && "Maria Lopez".equals(row.getSupplierName())));
    }

    @Test
    void wrapsUnexpectedFailures()
    {
        when(orderRepository.findByStatus(any())).thenThrow(new IllegalStateException("db"));
        assertFailed(() -> service.pending(MerchandiseReportFilterDTO.builder().build()));

        when(orderRepository.findByStatusAndDateRange(any(), any(), any())).thenThrow(new IllegalStateException("db"));
        assertFailed(() -> service.received(MerchandiseReportFilterDTO.builder()
                .startDate(LocalDate.of(2026, 9, 1))
                .endDate(LocalDate.of(2026, 9, 30))
                .build()));

        when(orderRepository.findAll()).thenThrow(new IllegalStateException("db"));
        assertFailed(() -> service.bySupplier(MerchandiseReportFilterDTO.builder().build()));
        assertFailed(() -> service.orderedNotSelling(MerchandiseReportFilterDTO.builder().build()));

        when(orderRepository.findByOrderDeliveryDateBetween(any(), any())).thenThrow(new IllegalStateException("db"));
        assertFailed(() -> service.orderedVsSold(MerchandiseReportFilterDTO.builder()
                .startDate(LocalDate.of(2026, 9, 1))
                .endDate(LocalDate.of(2026, 9, 30))
                .build()));
    }

    private void assertFailed(org.junit.jupiter.api.function.Executable executable)
    {
        BusinessException exception = assertThrows(BusinessException.class, executable);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getCode());
    }

    private Order order(String id, int supplier, LocalDate date, OrderStatusEnum status)
    {
        return Order.builder().id(id).supplier(supplier).orderDeliveryDate(date).status(status).build();
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
}
