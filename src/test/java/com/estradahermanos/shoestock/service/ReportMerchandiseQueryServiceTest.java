package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.request.MerchandiseReportFilterDTO;
import com.estradahermanos.shoestock.dto.response.OrderDetailResponseDTO;
import com.estradahermanos.shoestock.dto.response.PendingOrderReportDTO;
import com.estradahermanos.shoestock.mapper.OrderDetailMapper;
import com.estradahermanos.shoestock.mapper.ReportMerchandiseMapper;
import com.estradahermanos.shoestock.repository.entities.Order;
import com.estradahermanos.shoestock.repository.entities.OrderDetail;
import com.estradahermanos.shoestock.repository.entities.Shoe;
import com.estradahermanos.shoestock.repository.entities.ShoeStock;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
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
}
