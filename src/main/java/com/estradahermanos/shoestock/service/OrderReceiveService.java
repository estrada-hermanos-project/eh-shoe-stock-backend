package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.response.OrderResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.repository.entities.Order;
import com.estradahermanos.shoestock.repository.entities.OrderDetail;
import com.estradahermanos.shoestock.repository.repositories.OrderDetailRepository;
import com.estradahermanos.shoestock.repository.repositories.OrderRepository;
import com.estradahermanos.shoestock.utilities.ExceptionLog;
import com.estradahermanos.shoestock.utilities.OrderStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderReceiveService
{
    private final OrderRepository          orderRepository;
    private final OrderDetailRepository    orderDetailRepository;
    private final InventoryIncreaseService inventoryIncreaseService;
    private final OrderQueryService        orderQueryService;

    /** Marca la orden como RECIBIDA e incrementa el stock de cada linea. */
    @Transactional
    public OrderResponseDTO receive(String orderId)
    {
        log.info("Receiving order");
        try
        {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> BusinessException.builder()
                            .code(HttpStatus.NOT_FOUND)
                            .message("Order not found")
                            .build());

            if (order.getStatus() == OrderStatusEnum.RECIBIDA)
            {
                throw BusinessException.builder()
                        .code(HttpStatus.CONFLICT)
                        .message("Order is already received")
                        .build();
            }

            List<OrderDetail> details = orderDetailRepository.findByOrderId(orderId);
            if (details.isEmpty())
            {
                throw BusinessException.builder()
                        .code(HttpStatus.CONFLICT)
                        .message("Order has no details to receive")
                        .build();
            }

            for (OrderDetail detail : details)
            {
                inventoryIncreaseService.increaseByStockId(detail.getShoeStockId(), detail.getAmount());
            }

            order.setStatus(OrderStatusEnum.RECIBIDA);
            orderRepository.save(order);
            log.info("Order received");
            return orderQueryService.findById(orderId);
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to receive order", exception);
            throw BusinessException.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Could not receive order")
                    .build();
        }
    }
}
