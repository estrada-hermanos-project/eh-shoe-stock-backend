package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.response.OrderResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.repository.entities.Order;
import com.estradahermanos.shoestock.repository.entities.OrderDetail;
import com.estradahermanos.shoestock.repository.repositories.OrderDetailRepository;
import com.estradahermanos.shoestock.repository.repositories.OrderRepository;
import com.estradahermanos.shoestock.utilities.OrderStatusEnum;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderReceiveServiceTest
{
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderDetailRepository orderDetailRepository;

    @Mock
    private InventoryIncreaseService inventoryIncreaseService;

    @Mock
    private OrderQueryService orderQueryService;

    private OrderReceiveService service;

    @BeforeEach
    void setUp()
    {
        service = new OrderReceiveService(
                orderRepository, orderDetailRepository, inventoryIncreaseService, orderQueryService);
    }

    private Order pendingOrder()
    {
        return Order.builder()
                .id("ORDEN-101")
                .supplier(2)
                .orderDeliveryDate(LocalDate.of(2026, 9, 16))
                .status(OrderStatusEnum.PENDIENTE)
                .build();
    }

    @Test
    void receiveIncreasesStockAndMarksReceived()
    {
        when(orderRepository.findById("ORDEN-101")).thenReturn(Optional.of(pendingOrder()));
        when(orderDetailRepository.findByOrderId("ORDEN-101")).thenReturn(List.of(
                OrderDetail.builder().id(1).orderId("ORDEN-101").shoeStockId(15).amount(4).build(),
                OrderDetail.builder().id(2).orderId("ORDEN-101").shoeStockId(16).amount(2).build()));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderQueryService.findById("ORDEN-101")).thenReturn(OrderResponseDTO.builder()
                .orderId("ORDEN-101")
                .status(OrderStatusEnum.RECIBIDA)
                .details(List.of())
                .build());

        OrderResponseDTO result = service.receive("ORDEN-101");

        verify(inventoryIncreaseService).increaseByStockId(15, 4);
        verify(inventoryIncreaseService).increaseByStockId(16, 2);
        verify(orderRepository).save(any());
        assertEquals(OrderStatusEnum.RECIBIDA, result.getStatus());
    }

    @Test
    void receiveRejectsMissingOrder()
    {
        when(orderRepository.findById("ORDEN-101")).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> service.receive("ORDEN-101"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getCode());
        verify(inventoryIncreaseService, never()).increaseByStockId(anyInt(), anyInt());
    }

    @Test
    void receiveRejectsAlreadyReceived()
    {
        Order received = pendingOrder();
        received.setStatus(OrderStatusEnum.RECIBIDA);
        when(orderRepository.findById("ORDEN-101")).thenReturn(Optional.of(received));

        BusinessException exception = assertThrows(BusinessException.class, () -> service.receive("ORDEN-101"));

        assertEquals(HttpStatus.CONFLICT, exception.getCode());
        assertEquals("Order is already received", exception.getMessage());
        verify(inventoryIncreaseService, never()).increaseByStockId(anyInt(), anyInt());
    }

    @Test
    void receiveRejectsOrderWithoutDetails()
    {
        when(orderRepository.findById("ORDEN-101")).thenReturn(Optional.of(pendingOrder()));
        when(orderDetailRepository.findByOrderId("ORDEN-101")).thenReturn(List.of());

        BusinessException exception = assertThrows(BusinessException.class, () -> service.receive("ORDEN-101"));

        assertEquals(HttpStatus.CONFLICT, exception.getCode());
        assertEquals("Order has no details to receive", exception.getMessage());
        verify(inventoryIncreaseService, never()).increaseByStockId(anyInt(), anyInt());
        verify(orderRepository, never()).save(any());
    }
}
