package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.response.OrderDetailResponseDTO;
import com.estradahermanos.shoestock.dto.response.OrderResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.mapper.OrderDetailMapper;
import com.estradahermanos.shoestock.mapper.OrderMapper;
import com.estradahermanos.shoestock.repository.entities.Order;
import com.estradahermanos.shoestock.repository.entities.OrderDetail;
import com.estradahermanos.shoestock.repository.entities.Shoe;
import com.estradahermanos.shoestock.repository.entities.ShoeStock;
import com.estradahermanos.shoestock.repository.entities.Supplier;
import com.estradahermanos.shoestock.repository.repositories.OrderDetailRepository;
import com.estradahermanos.shoestock.repository.repositories.OrderRepository;
import com.estradahermanos.shoestock.repository.repositories.ShoeRepository;
import com.estradahermanos.shoestock.repository.repositories.ShoeStockRepository;
import com.estradahermanos.shoestock.repository.repositories.SupplierRepository;
import com.estradahermanos.shoestock.utilities.OrderStatusEnum;
import com.estradahermanos.shoestock.utilities.OrderValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderQueryServiceTest
{
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderDetailRepository orderDetailRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ShoeStockRepository shoeStockRepository;

    @Mock
    private ShoeRepository shoeRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderDetailMapper orderDetailMapper;

    private OrderQueryService service;

    @BeforeEach
    void setUp()
    {
        service = new OrderQueryService(
                orderRepository,
                orderDetailRepository,
                supplierRepository,
                shoeStockRepository,
                shoeRepository,
                orderMapper,
                orderDetailMapper,
                new OrderValidator());
    }

    private Order order()
    {
        return Order.builder()
                .id("ORDEN-101")
                .supplier(2)
                .orderDeliveryDate(LocalDate.of(2026, 9, 16))
                .status(OrderStatusEnum.PENDIENTE)
                .build();
    }

    private void stubResolution()
    {
        when(orderDetailRepository.findByOrderIdIn(anyCollection())).thenReturn(List.of(
                OrderDetail.builder().id(1).orderId("ORDEN-101").shoeStockId(15).amount(4).build()));
        when(shoeStockRepository.findAllById(anyCollection())).thenReturn(List.of(
                ShoeStock.builder().id(15).shoeId("OXF-001").color("Negro").size(40).stock(3).minStock(0).build()));
        when(shoeRepository.findAllById(anyCollection())).thenReturn(List.of(
                Shoe.builder().code("OXF-001").type("Caballero").name("Oxford clasico").supplier(2).build()));
        when(supplierRepository.findAll()).thenReturn(List.of(
                Supplier.builder().id(2).fullName("Maria Lopez").phone("55512345").build()));
        when(orderDetailMapper.toResponse(any(), any())).thenReturn(
                OrderDetailResponseDTO.builder().shoeStockId(15).shoeName("Oxford clasico").amount(4).build());
        when(orderMapper.toResponse(any(), any(), any())).thenReturn(
                OrderResponseDTO.builder()
                        .orderId("ORDEN-101")
                        .supplierName("Maria Lopez")
                        .status(OrderStatusEnum.PENDIENTE)
                        .creationDate(LocalDate.of(2026, 9, 16))
                        .details(List.of(OrderDetailResponseDTO.builder()
                                .shoeStockId(15)
                                .shoeName("Oxford clasico")
                                .amount(4)
                                .build()))
                        .build());
    }

    @Test
    void findByIdReturnsOrder()
    {
        when(orderRepository.findById("ORDEN-101")).thenReturn(Optional.of(order()));
        stubResolution();

        OrderResponseDTO result = service.findById("ORDEN-101");

        assertEquals("ORDEN-101", result.getOrderId());
        assertEquals("Maria Lopez", result.getSupplierName());
        assertEquals(1, result.getDetails().size());
        assertEquals("Oxford clasico", result.getDetails().get(0).getShoeName());
    }

    @Test
    void findByIdRejectsMissingOrder()
    {
        when(orderRepository.findById("ORDEN-101")).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> service.findById("ORDEN-101"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getCode());
        assertEquals("Order not found", exception.getMessage());
        verify(orderDetailRepository, never()).findByOrderIdIn(anyCollection());
    }

    @Test
    void findByDateRangeReturnsOrders()
    {
        LocalDate start = LocalDate.of(2026, 9, 1);
        LocalDate end   = LocalDate.of(2026, 9, 16);
        when(orderRepository.findByOrderDeliveryDateBetween(start, end)).thenReturn(List.of(order()));
        stubResolution();

        List<OrderResponseDTO> result = service.findByDateRange(start, end);

        assertEquals(1, result.size());
        assertEquals("ORDEN-101", result.get(0).getOrderId());
    }

    @Test
    void findByDateRangeRejectsInvertedDates()
    {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.findByDateRange(LocalDate.of(2026, 9, 16), LocalDate.of(2026, 9, 1)));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
        assertEquals("start_date must not be after end_date", exception.getMessage());
        verify(orderRepository, never()).findByOrderDeliveryDateBetween(any(), any());
    }

    @Test
    void findByDateRangeReturnsEmptyList()
    {
        LocalDate start = LocalDate.of(2026, 9, 1);
        LocalDate end   = LocalDate.of(2026, 9, 16);
        when(orderRepository.findByOrderDeliveryDateBetween(start, end)).thenReturn(List.of());

        List<OrderResponseDTO> result = service.findByDateRange(start, end);

        assertTrue(result.isEmpty());
        verify(orderDetailRepository, never()).findByOrderIdIn(anyCollection());
    }

    @Test
    void findByStatusReturnsOrders()
    {
        when(orderRepository.findByStatus(OrderStatusEnum.PENDIENTE)).thenReturn(List.of(order()));
        stubResolution();

        List<OrderResponseDTO> result = service.findByStatus("PENDIENTE");

        assertEquals(1, result.size());
        assertEquals(OrderStatusEnum.PENDIENTE, result.get(0).getStatus());
    }

    @Test
    void findByStatusRejectsInvalidValue()
    {
        BusinessException exception = assertThrows(BusinessException.class, () -> service.findByStatus("CANCELADO"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
        verify(orderRepository, never()).findByStatus(any());
    }

    @Test
    void findBySupplierReturnsOrders()
    {
        when(supplierRepository.existsById(2)).thenReturn(true);
        when(orderRepository.findBySupplier(2)).thenReturn(List.of(order()));
        stubResolution();

        List<OrderResponseDTO> result = service.findBySupplier(2);

        assertEquals(1, result.size());
        assertEquals("Maria Lopez", result.get(0).getSupplierName());
    }

    @Test
    void findBySupplierRejectsMissingSupplier()
    {
        when(supplierRepository.existsById(99)).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.findBySupplier(99));

        assertEquals(HttpStatus.NOT_FOUND, exception.getCode());
        assertEquals("Supplier not found", exception.getMessage());
        verify(orderRepository, never()).findBySupplier(any());
    }
}
