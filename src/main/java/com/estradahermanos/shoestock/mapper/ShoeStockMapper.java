package com.estradahermanos.shoestock.mapper;

import com.estradahermanos.shoestock.dto.response.ShoeStockResponseDTO;
import com.estradahermanos.shoestock.repository.entities.ShoeStock;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShoeStockMapper
{
    ShoeStockResponseDTO toResponse(ShoeStock shoeStock);
}
