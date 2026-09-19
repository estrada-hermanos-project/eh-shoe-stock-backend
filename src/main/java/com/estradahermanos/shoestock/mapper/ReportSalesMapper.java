package com.estradahermanos.shoestock.mapper;

import com.estradahermanos.shoestock.dto.response.PeriodSaleDTO;
import com.estradahermanos.shoestock.dto.response.ProductSalesRankDTO;
import com.estradahermanos.shoestock.repository.entities.Sale;
import com.estradahermanos.shoestock.repository.entities.ShoeStock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDate;

@Mapper(componentModel = "spring")
public interface ReportSalesMapper
{
    @Mapping(target = "shoeStockId", source = "variant.id")
    @Mapping(target = "shoeName", source = "shoeName")
    @Mapping(target = "color", source = "variant.color")
    @Mapping(target = "size", source = "variant.size")
    @Mapping(target = "amountSold", source = "amountSold")
    @Mapping(target = "currentStock", source = "currentStock")
    @Mapping(target = "lastSaleDate", source = "lastSaleDate")
    ProductSalesRankDTO toRank(ShoeStock variant, String shoeName, Long amountSold,
                               Integer currentStock, LocalDate lastSaleDate);

    @Mapping(target = "shoeStockId", source = "sale.shoeStockId")
    @Mapping(target = "shoeName", source = "shoeName")
    @Mapping(target = "color", source = "color")
    @Mapping(target = "size", source = "sale.size")
    @Mapping(target = "amount", source = "sale.amount")
    @Mapping(target = "saleDate", source = "sale.saleDate")
    PeriodSaleDTO toPeriodSale(Sale sale, String shoeName, String color);
}
