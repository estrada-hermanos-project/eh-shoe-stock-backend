package com.estradahermanos.shoestock.mapper;

import com.estradahermanos.shoestock.dto.request.CreateVariantRequestDTO;
import com.estradahermanos.shoestock.dto.response.ShoeStockResponseDTO;
import com.estradahermanos.shoestock.dto.response.StockQueryResponseDTO;
import com.estradahermanos.shoestock.dto.response.SupplierVariantResponseDTO;
import com.estradahermanos.shoestock.repository.entities.ShoeStock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ShoeStockMapper
{
    ShoeStockResponseDTO toResponse(ShoeStock shoeStock);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "stock", expression = "java(0)")
    @Mapping(target = "minStock", expression = "java(0)")
    ShoeStock toNewVariant(CreateVariantRequestDTO request);

    @Mapping(target = "name", source = "name")
    StockQueryResponseDTO toStockQuery(ShoeStock shoeStock, String name);

    @Mapping(target = "shoeStockId", source = "variant.id")
    @Mapping(target = "shoeCode", source = "variant.shoeId")
    @Mapping(target = "shoeName", source = "shoeName")
    @Mapping(target = "color", source = "variant.color")
    @Mapping(target = "size", source = "variant.size")
    SupplierVariantResponseDTO toSupplierVariant(ShoeStock variant, String shoeName);
}
