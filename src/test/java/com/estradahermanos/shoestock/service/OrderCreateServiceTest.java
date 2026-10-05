package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.request.CreateOrderRequestDTO;
import com.estradahermanos.shoestock.dto.response.OrderResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.mapper.OrderMapper;
import com.estradahermanos.shoestock.repository.entities.Order;
import com.estradahermanos.shoestock.repository.entities.Supplier;
import com.estradahermanos.shoestock.repository.repositories.OrderRepository;
import com.estradahermanos.shoestock.repository.repositories.SupplierRepository;
import com.estradahermanos.shoestock.utilities.BusinessDate;
import com.estradahermanos.shoestock.utilities.OrderStatusEnum;
import com.estradahermanos.shoestock.utilities.OrderValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderCreateServiceTest
{
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private OrderMapper orderMapper;

    private static final LocalDate BUSINESS_DAY = LocalDate.of(2026, 10, 1);

    private OrderCreateService service;

    @BeforeEach
    void setUp()
    {
        service = serviceAt(LocalTime.of(10, 0));
    }

    private OrderCreateService serviceAt(LocalTime guatemalaTime)
    {
        Clock clock = Clock.fixed(
                BUSINESS_DAY.atTime(guatemalaTime).atZone(BusinessDate.ZONE).toInstant(),
                BusinessDate.ZONE);
        return new OrderCreateService(
                orderRepository,
                supplierRepository,
                new OrderValidator(),
                orderMapper,
                new BusinessDate(clock));
    }

    private CreateOrderRequestDTO request()
    {
        return CreateOrderRequestDTO.builder()
                .id("ORDEN-101")
                .supplierId(2)
                .build();
    }

    @Test
    void createPersistsPendingOrderWithTodayDate()
    {
        when(orderRepository.existsById("ORDEN-101")).thenReturn(false);
        when(supplierRepository.findById(2)).thenReturn(Optional.of(
                Supplier.builder().id(2).fullName("Maria Lopez").phone("55512345").build()));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.toResponse(any(), eq("Maria Lopez"), eq(List.of())))
                .thenReturn(OrderResponseDTO.builder()
                        .orderId("ORDEN-101")
                        .supplierName("Maria Lopez")
                        .status(OrderStatusEnum.PENDIENTE)
                        .creationDate(BUSINESS_DAY)
                        .details(List.of())
                        .build());

        OrderResponseDTO result = service.create(request());

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        Order saved = captor.getValue();
        assertEquals("ORDEN-101", saved.getId());
        assertEquals(2, saved.getSupplier());
        assertEquals(OrderStatusEnum.PENDIENTE, saved.getStatus());
        assertEquals(BUSINESS_DAY, saved.getOrderDeliveryDate());
        assertEquals("Maria Lopez", result.getSupplierName());
        assertTrue(result.getDetails().isEmpty());
    }

    @Test
    void createKeepsGuatemalaDateAfterSixPm()
    {
        OrderCreateService eveningService = serviceAt(LocalTime.of(18, 30));
        when(orderRepository.existsById("ORDEN-101")).thenReturn(false);
        when(supplierRepository.findById(2)).thenReturn(Optional.of(
                Supplier.builder().id(2).fullName("Maria Lopez").phone("55512345").build()));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.toResponse(any(), eq("Maria Lopez"), eq(List.of())))
                .thenReturn(OrderResponseDTO.builder()
                        .orderId("ORDEN-101")
                        .creationDate(BUSINESS_DAY)
                        .details(List.of())
                        .build());

        eveningService.create(request());

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        assertEquals(BUSINESS_DAY, captor.getValue().getOrderDeliveryDate());
    }

    @Test
    void createRejectsNullRequest()
    {
        BusinessException exception = assertThrows(BusinessException.class, () -> service.create(null));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createRejectsBlankId()
    {
        CreateOrderRequestDTO request = request();
        request.setId("  ");

        BusinessException exception = assertThrows(BusinessException.class, () -> service.create(request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
        verify(orderRepository, never()).existsById(any());
    }

    @Test
    void createRejectsDuplicateId()
    {
        when(orderRepository.existsById("ORDEN-101")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.create(request()));

        assertEquals(HttpStatus.CONFLICT, exception.getCode());
        assertEquals("An order with this id already exists", exception.getMessage());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createRejectsMissingSupplier()
    {
        when(orderRepository.existsById("ORDEN-101")).thenReturn(false);
        when(supplierRepository.findById(2)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> service.create(request()));

        assertEquals(HttpStatus.NOT_FOUND, exception.getCode());
        assertEquals("Supplier not found", exception.getMessage());
        verify(orderRepository, never()).save(any());
    }
}
