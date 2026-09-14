package com.estradahermanos.shoestock.mapper;

import com.estradahermanos.shoestock.dto.response.ShoeStockResponseDTO;
import com.estradahermanos.shoestock.dto.response.StockQueryResponseDTO;
import com.estradahermanos.shoestock.repository.entities.ShoeStock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ShoeStockMapper
{
    ShoeStockResponseDTO toResponse(ShoeStock shoeStock);

    @Mapping(target = "name", source = "name")
    StockQueryResponseDTO toStockQuery(ShoeStock shoeStock, String name);
}
