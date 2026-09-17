package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.request.CreateOrderRequestDTO;
import com.estradahermanos.shoestock.dto.response.OrderResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.mapper.OrderMapper;
import com.estradahermanos.shoestock.repository.entities.Order;
import com.estradahermanos.shoestock.repository.entities.Supplier;
import com.estradahermanos.shoestock.repository.repositories.OrderRepository;
import com.estradahermanos.shoestock.repository.repositories.SupplierRepository;
import com.estradahermanos.shoestock.utilities.ExceptionLog;
import com.estradahermanos.shoestock.utilities.OrderStatusEnum;
import com.estradahermanos.shoestock.utilities.OrderValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCreateService
{
    private final OrderRepository    orderRepository;
    private final SupplierRepository supplierRepository;
    private final OrderValidator     orderValidator;
    private final OrderMapper        orderMapper;

    /** Crea la cabecera del pedido con status PENDIENTE y fecha actual. */
    @Transactional
    public OrderResponseDTO create(CreateOrderRequestDTO request)
    {
        log.info("Creating order");
        try
        {
            if (request == null)
            {
                throw BusinessException.builder()
                        .code(HttpStatus.BAD_REQUEST)
                        .message("Request body is required")
                        .build();
            }

            orderValidator.validateCreate(request.getId());

            if (orderRepository.existsById(request.getId()))
            {
                throw BusinessException.builder()
                        .code(HttpStatus.CONFLICT)
                        .message("An order with this id already exists")
                        .build();
            }

            Supplier supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> BusinessException.builder()
                            .code(HttpStatus.NOT_FOUND)
                            .message("Supplier not found")
                            .build());

            Order saved = orderRepository.save(Order.builder()
                    .id(request.getId())
                    .supplier(request.getSupplierId())
                    .orderDeliveryDate(LocalDate.now())
                    .status(OrderStatusEnum.PENDIENTE)
                    .build());

            log.info("Order created");
            return orderMapper.toResponse(saved, supplier.getFullName(), List.of());
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to create order", exception);
            throw BusinessException.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Could not create order")
                    .build();
        }
    }
}
