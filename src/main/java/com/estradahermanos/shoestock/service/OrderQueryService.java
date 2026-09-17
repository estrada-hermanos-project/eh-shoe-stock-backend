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
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryService
{
    private final OrderRepository       orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final SupplierRepository    supplierRepository;
    private final ShoeStockRepository   shoeStockRepository;
    private final ShoeRepository        shoeRepository;
    private final OrderMapper           orderMapper;
    private final OrderDetailMapper     orderDetailMapper;
    private final OrderValidator        orderValidator;

    /** Consulta una orden completa por id. */
    public OrderResponseDTO findById(String orderId)
    {
        log.info("Querying order by id");
        try
        {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> BusinessException.builder()
                            .code(HttpStatus.NOT_FOUND)
                            .message("Order not found")
                            .build());
            return toResponseList(List.of(order)).get(0);
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to query order", exception);
            throw BusinessException.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Could not query order")
                    .build();
        }
    }

    /** Lista ordenes entre dos fechas de creacion, inclusive. */
    public List<OrderResponseDTO> findByDateRange(LocalDate startDate, LocalDate endDate)
    {
        log.info("Querying orders by date range");
        try
        {
            if (startDate.isAfter(endDate))
            {
                throw BusinessException.builder()
                        .code(HttpStatus.BAD_REQUEST)
                        .message("start_date must not be after end_date")
                        .build();
            }
            return toResponseList(orderRepository.findByOrderDeliveryDateBetween(startDate, endDate));
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to query orders by date range", exception);
            throw BusinessException.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Could not query orders")
                    .build();
        }
    }

    /** Lista ordenes por status PENDIENTE o RECIBIDA. */
    public List<OrderResponseDTO> findByStatus(String status)
    {
        log.info("Querying orders by status");
        try
        {
            OrderStatusEnum parsed = orderValidator.parseStatus(status);
            return toResponseList(orderRepository.findByStatus(parsed));
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to query orders by status", exception);
            throw BusinessException.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Could not query orders")
                    .build();
        }
    }

    /** Lista ordenes de un proveedor existente, ordenadas por fecha de creacion. */
    public List<OrderResponseDTO> findBySupplier(Integer supplierId)
    {
        log.info("Querying orders by supplier");
        try
        {
            if (!supplierRepository.existsById(supplierId))
            {
                throw BusinessException.builder()
                        .code(HttpStatus.NOT_FOUND)
                        .message("Supplier not found")
                        .build();
            }
            return toResponseList(orderRepository.findBySupplier(supplierId));
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to query orders by supplier", exception);
            throw BusinessException.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Could not query orders")
                    .build();
        }
    }

    /** Resuelve nombre de proveedor y zapatos con mapas para no disparar N+1. */
    private List<OrderResponseDTO> toResponseList(List<Order> orders)
    {
        if (orders.isEmpty())
        {
            return List.of();
        }

        List<String> orderIds = orders.stream()
                .map(Order::getId)
                .toList();

        List<OrderDetail> details = orderDetailRepository.findByOrderIdIn(orderIds);

        Map<String, List<OrderDetail>> detailsByOrder = details.stream()
                .collect(Collectors.groupingBy(OrderDetail::getOrderId));

        List<Integer> stockIds = details.stream()
                .map(OrderDetail::getShoeStockId)
                .distinct()
                .toList();

        Map<Integer, ShoeStock> variants = stockIds.isEmpty()
                ? Map.of()
                : shoeStockRepository.findAllById(stockIds).stream()
                        .collect(Collectors.toMap(ShoeStock::getId, variant -> variant));

        List<String> shoeIds = variants.values().stream()
                .map(ShoeStock::getShoeId)
                .distinct()
                .toList();

        Map<String, String> shoeNames = shoeIds.isEmpty()
                ? Map.of()
                : shoeRepository.findAllById(shoeIds).stream()
                        .collect(Collectors.toMap(Shoe::getCode, Shoe::getName));

        Map<Integer, String> supplierNames = supplierRepository.findAll().stream()
                .collect(Collectors.toMap(Supplier::getId, Supplier::getFullName));

        return orders.stream()
                .map(order ->
                {
                    List<OrderDetailResponseDTO> detailDtos = detailsByOrder
                            .getOrDefault(order.getId(), List.of())
                            .stream()
                            .map(detail ->
                            {
                                ShoeStock variant   = variants.get(detail.getShoeStockId());
                                String    shoeName  = variant != null ? shoeNames.get(variant.getShoeId()) : null;
                                return orderDetailMapper.toResponse(detail, shoeName);
                            })
                            .toList();
                    return orderMapper.toResponse(order, supplierNames.get(order.getSupplier()), detailDtos);
                })
                .toList();
    }
}
