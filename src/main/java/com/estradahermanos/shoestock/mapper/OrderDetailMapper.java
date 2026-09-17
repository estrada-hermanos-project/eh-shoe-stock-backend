package com.estradahermanos.shoestock.mapper;

import com.estradahermanos.shoestock.dto.response.OrderDetailResponseDTO;
import com.estradahermanos.shoestock.repository.entities.OrderDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderDetailMapper
{
    @Mapping(target = "shoeStockId", source = "detail.shoeStockId")
    @Mapping(target = "shoeName", source = "shoeName")
    @Mapping(target = "amount", source = "detail.amount")
    OrderDetailResponseDTO toResponse(OrderDetail detail, String shoeName);
}
