package com.estradahermanos.shoestock.mapper;

import com.estradahermanos.shoestock.dto.response.SaleResponseDTO;
import com.estradahermanos.shoestock.repository.entities.Sale;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SaleMapper
{
    @Mapping(target = "stock", source = "sale.amount")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "color", source = "color")
    SaleResponseDTO toResponse(Sale sale, String name, String color);
}
