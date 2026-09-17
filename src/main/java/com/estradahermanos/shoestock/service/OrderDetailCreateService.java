package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.request.CreateOrderDetailRequestDTO;
import com.estradahermanos.shoestock.dto.response.OrderResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.repository.entities.Order;
import com.estradahermanos.shoestock.repository.entities.OrderDetail;
import com.estradahermanos.shoestock.repository.repositories.OrderDetailRepository;
import com.estradahermanos.shoestock.repository.repositories.OrderRepository;
import com.estradahermanos.shoestock.repository.repositories.ShoeStockRepository;
import com.estradahermanos.shoestock.utilities.ExceptionLog;
import com.estradahermanos.shoestock.utilities.OrderStatusEnum;
import com.estradahermanos.shoestock.utilities.OrderValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderDetailCreateService
{
    private final OrderRepository       orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ShoeStockRepository   shoeStockRepository;
    private final OrderValidator        orderValidator;
    private final OrderQueryService     orderQueryService;

    /** Agrega una linea de detalle a una orden PENDIENTE. */
    @Transactional
    public OrderResponseDTO create(String orderId, CreateOrderDetailRequestDTO request)
    {
        log.info("Creating order detail");
        try
        {
            if (request == null)
            {
                throw BusinessException.builder()
                        .code(HttpStatus.BAD_REQUEST)
                        .message("Request body is required")
                        .build();
            }

            orderValidator.validateDetail(request.getAmount());

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> BusinessException.builder()
                            .code(HttpStatus.NOT_FOUND)
                            .message("Order not found")
                            .build());

            if (order.getStatus() != OrderStatusEnum.PENDIENTE)
            {
                throw BusinessException.builder()
                        .code(HttpStatus.CONFLICT)
                        .message("Order is not pending")
                        .build();
            }

            shoeStockRepository.findById(request.getShoeStockId())
                    .orElseThrow(() -> BusinessException.builder()
                            .code(HttpStatus.NOT_FOUND)
                            .message("Stock not found")
                            .build());

            orderDetailRepository.save(OrderDetail.builder()
                    .orderId(orderId)
                    .shoeStockId(request.getShoeStockId())
                    .amount(request.getAmount())
                    .build());

            log.info("Order detail created");
            return orderQueryService.findById(orderId);
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to create order detail", exception);
            throw BusinessException.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Could not create order detail")
                    .build();
        }
    }
}
