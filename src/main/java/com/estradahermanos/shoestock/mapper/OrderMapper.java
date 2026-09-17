package com.estradahermanos.shoestock.mapper;

import com.estradahermanos.shoestock.dto.response.OrderDetailResponseDTO;
import com.estradahermanos.shoestock.dto.response.OrderResponseDTO;
import com.estradahermanos.shoestock.repository.entities.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper
{
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "supplierName", source = "supplierName")
    @Mapping(target = "status", source = "order.status")
    @Mapping(target = "creationDate", source = "order.orderDeliveryDate")
    @Mapping(target = "details", source = "details")
    OrderResponseDTO toResponse(Order order, String supplierName, List<OrderDetailResponseDTO> details);
}
