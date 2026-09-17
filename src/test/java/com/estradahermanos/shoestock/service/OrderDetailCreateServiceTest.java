package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.request.CreateOrderDetailRequestDTO;
import com.estradahermanos.shoestock.dto.response.OrderResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.repository.entities.Order;
import com.estradahermanos.shoestock.repository.entities.OrderDetail;
import com.estradahermanos.shoestock.repository.entities.ShoeStock;
import com.estradahermanos.shoestock.repository.repositories.OrderDetailRepository;
import com.estradahermanos.shoestock.repository.repositories.OrderRepository;
import com.estradahermanos.shoestock.repository.repositories.ShoeStockRepository;
import com.estradahermanos.shoestock.utilities.OrderStatusEnum;
import com.estradahermanos.shoestock.utilities.OrderValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderDetailCreateServiceTest
{
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderDetailRepository orderDetailRepository;

    @Mock
    private ShoeStockRepository shoeStockRepository;

    @Mock
    private OrderQueryService orderQueryService;

    private OrderDetailCreateService service;

    @BeforeEach
    void setUp()
    {
        service = new OrderDetailCreateService(
                orderRepository,
                orderDetailRepository,
                shoeStockRepository,
                new OrderValidator(),
                orderQueryService);
    }

    private CreateOrderDetailRequestDTO request()
    {
        return CreateOrderDetailRequestDTO.builder()
                .shoeStockId(15)
                .amount(4)
                .build();
    }

    private Order pendingOrder()
    {
        return Order.builder()
                .id("ORDEN-101")
                .supplier(2)
                .orderDeliveryDate(LocalDate.now())
                .status(OrderStatusEnum.PENDIENTE)
                .build();
    }

    @Test
    void createPersistsDetailOnPendingOrder()
    {
        when(orderRepository.findById("ORDEN-101")).thenReturn(Optional.of(pendingOrder()));
        when(shoeStockRepository.findById(15)).thenReturn(Optional.of(
                ShoeStock.builder().id(15).shoeId("OXF-001").color("Negro").size(40).stock(3).minStock(0).build()));
        when(orderDetailRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderQueryService.findById("ORDEN-101")).thenReturn(OrderResponseDTO.builder()
                .orderId("ORDEN-101")
                .status(OrderStatusEnum.PENDIENTE)
                .details(List.of())
                .build());

        service.create("ORDEN-101", request());

        ArgumentCaptor<OrderDetail> captor = ArgumentCaptor.forClass(OrderDetail.class);
        verify(orderDetailRepository).save(captor.capture());
        assertEquals("ORDEN-101", captor.getValue().getOrderId());
        assertEquals(15, captor.getValue().getShoeStockId());
        assertEquals(4, captor.getValue().getAmount());
        verify(orderQueryService).findById("ORDEN-101");
    }

    @Test
    void createRejectsNullRequest()
    {
        BusinessException exception = assertThrows(BusinessException.class, () -> service.create("ORDEN-101", null));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
        verify(orderDetailRepository, never()).save(any());
    }

    @Test
    void createRejectsAmountBelowOne()
    {
        CreateOrderDetailRequestDTO request = request();
        request.setAmount(0);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.create("ORDEN-101", request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
        verify(orderRepository, never()).findById(any());
    }

    @Test
    void createRejectsMissingOrder()
    {
        when(orderRepository.findById("ORDEN-101")).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.create("ORDEN-101", request()));

        assertEquals(HttpStatus.NOT_FOUND, exception.getCode());
        assertEquals("Order not found", exception.getMessage());
        verify(orderDetailRepository, never()).save(any());
    }

    @Test
    void createRejectsReceivedOrder()
    {
        Order received = pendingOrder();
        received.setStatus(OrderStatusEnum.RECIBIDA);
        when(orderRepository.findById("ORDEN-101")).thenReturn(Optional.of(received));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.create("ORDEN-101", request()));

        assertEquals(HttpStatus.CONFLICT, exception.getCode());
        assertEquals("Order is not pending", exception.getMessage());
        verify(orderDetailRepository, never()).save(any());
    }

    @Test
    void createRejectsMissingStock()
    {
        when(orderRepository.findById("ORDEN-101")).thenReturn(Optional.of(pendingOrder()));
        when(shoeStockRepository.findById(15)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.create("ORDEN-101", request()));

        assertEquals(HttpStatus.NOT_FOUND, exception.getCode());
        assertEquals("Stock not found", exception.getMessage());
        verify(orderDetailRepository, never()).save(any());
    }
}
