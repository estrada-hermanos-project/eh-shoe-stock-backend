package com.estradahermanos.shoestock.mapper;

import com.estradahermanos.shoestock.dto.response.InventoryStatusDTO;
import com.estradahermanos.shoestock.dto.response.LowStockDTO;
import com.estradahermanos.shoestock.repository.entities.ShoeStock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReportInventoryMapper
{
    @Mapping(target = "shoeStockId", source = "variant.id")
    @Mapping(target = "shoeName", source = "shoeName")
    @Mapping(target = "color", source = "variant.color")
    @Mapping(target = "size", source = "variant.size")
    @Mapping(target = "stock", source = "variant.stock")
    InventoryStatusDTO toStatus(ShoeStock variant, String shoeName);

    @Mapping(target = "shoeStockId", source = "variant.id")
    @Mapping(target = "shoeName", source = "shoeName")
    @Mapping(target = "color", source = "variant.color")
    @Mapping(target = "size", source = "variant.size")
    @Mapping(target = "stock", source = "variant.stock")
    @Mapping(target = "minStock", source = "variant.minStock")
    @Mapping(target = "pairsBelowMin", source = "pairsBelowMin")
    @Mapping(target = "supplierName", source = "supplierName")
    LowStockDTO toLowStock(ShoeStock variant, String shoeName, Integer pairsBelowMin, String supplierName);
}
