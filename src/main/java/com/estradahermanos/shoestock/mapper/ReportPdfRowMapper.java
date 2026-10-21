package com.estradahermanos.shoestock.mapper;

import com.estradahermanos.shoestock.dto.response.InventoryGroupDTO;
import com.estradahermanos.shoestock.dto.response.InventoryStatusDTO;
import com.estradahermanos.shoestock.dto.response.LowStockDTO;
import com.estradahermanos.shoestock.dto.response.NewStyleDTO;
import com.estradahermanos.shoestock.dto.response.OrderedNotSellingDTO;
import com.estradahermanos.shoestock.dto.response.OrderedVsSoldDTO;
import com.estradahermanos.shoestock.dto.response.OrdersBySupplierDTO;
import com.estradahermanos.shoestock.dto.response.OutOfStockDTO;
import com.estradahermanos.shoestock.dto.response.PendingOrderReportDTO;
import com.estradahermanos.shoestock.dto.response.PeriodSaleDTO;
import com.estradahermanos.shoestock.dto.response.ProductSalesRankDTO;
import com.estradahermanos.shoestock.dto.response.ReceivedOrderReportDTO;
import com.estradahermanos.shoestock.dto.response.RestockSuggestionDTO;
import com.estradahermanos.shoestock.dto.response.SalesByCategoryDTO;
import com.estradahermanos.shoestock.dto.response.SalesBySizeDTO;
import com.estradahermanos.shoestock.dto.response.SalesBySupplierDTO;
import com.estradahermanos.shoestock.dto.response.SalesVolumeDTO;
import com.estradahermanos.shoestock.dto.response.SlowMoverDTO;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Objects;

@Mapper(componentModel = "spring")
public interface ReportPdfRowMapper
{
    default List<String> toMetricRow(String metric, Object value)
    {
        return List.of(text(metric), text(value));
    }

    default List<String> toNewStyleRow(NewStyleDTO style)
    {
        return List.of(
                text(style.getCode()),
                text(style.getName()),
                text(style.getType()),
                text(style.getSupplierName()),
                text(style.getCreatedAt()));
    }

    default List<String> toTopProductRow(ProductSalesRankDTO product)
    {
        return List.of(
                text(product.getShoeCode()),
                text(product.getShoeName()),
                text(product.getColor()),
                text(product.getSize()),
                text(product.getAmountSold()),
                text(product.getCurrentStock()));
    }

    default List<String> toPeriodSaleRow(PeriodSaleDTO sale)
    {
        return List.of(
                text(sale.getSaleDate()),
                text(sale.getShoeName()),
                text(sale.getColor()),
                text(sale.getSize()),
                text(sale.getAmount()));
    }

    default List<String> toCategoryRow(SalesByCategoryDTO row)
    {
        return List.of(text(row.getType()), text(row.getAmountSold()), text(row.getSaleCount()), percent(row.getSharePercent()));
    }

    default List<String> toSizeRow(SalesBySizeDTO row)
    {
        return List.of(text(row.getSize()), text(row.getType()), text(row.getAmountSold()), text(row.getCurrentStock()));
    }

    default List<String> toSalesSupplierRow(SalesBySupplierDTO row)
    {
        return List.of(
                text(row.getSupplierName()),
                text(row.getAmountSold()),
                text(row.getStylesSold()),
                percent(row.getSharePercent()));
    }

    default List<String> toVolumeRow(SalesVolumeDTO row)
    {
        return List.of(text(row.getPeriod()), text(row.getAmountSold()), text(row.getSaleCount()));
    }

    default List<String> toInventoryStatusRow(InventoryStatusDTO row)
    {
        return List.of(text(row.getShoeName()), text(row.getColor()), text(row.getSize()), text(row.getStock()));
    }

    default List<String> toInventoryGroupRow(InventoryGroupDTO row)
    {
        return List.of(
                text(row.getGroup()),
                text(row.getVariantCount()),
                text(row.getPairsInStock()),
                text(row.getLowStockCount()),
                text(row.getOutOfStockCount()));
    }

    default List<String> toLowStockRow(LowStockDTO row)
    {
        return List.of(
                text(row.getShoeName()),
                text(row.getColor()),
                text(row.getSize()),
                text(row.getStock()),
                text(row.getMinStock()),
                text(row.getSupplierName()));
    }

    default List<String> toOutOfStockRow(OutOfStockDTO row)
    {
        return List.of(
                text(row.getShoeName()),
                text(row.getColor()),
                text(row.getSize()),
                text(row.getType()),
                yesNo(row.getHasPendingOrder()));
    }

    default List<String> toSlowMoverRow(SlowMoverDTO row)
    {
        return List.of(
                text(row.getShoeName()),
                text(row.getColor()),
                text(row.getSize()),
                text(row.getStock()),
                neverSold(row.getLastSaleDate()),
                text(row.getDaysWithoutSale()));
    }

    default List<String> toRestockRow(RestockSuggestionDTO row)
    {
        return List.of(
                text(row.getShoeName()),
                text(row.getColor()),
                text(row.getSize()),
                text(row.getAmountSold()),
                text(row.getStock()),
                text(row.getAdvice()));
    }

    default List<String> toPendingOrderRow(PendingOrderReportDTO row)
    {
        return List.of(
                text(row.getOrderId()),
                text(row.getSupplierName()),
                text(row.getDaysPending()),
                text(row.getTotalPairs()));
    }

    default List<String> toReceivedOrderRow(ReceivedOrderReportDTO row)
    {
        return List.of(
                text(row.getOrderId()),
                text(row.getSupplierName()),
                text(row.getCreationDate()),
                text(row.getTotalPairs()));
    }

    default List<String> toOrdersSupplierRow(OrdersBySupplierDTO row)
    {
        return List.of(
                text(row.getSupplierName()),
                text(row.getPendingCount()),
                text(row.getReceivedCount()),
                text(row.getPairsRequested()),
                text(row.getPairsReceived()));
    }

    default List<String> toOrderedVsSoldRow(OrderedVsSoldDTO row)
    {
        return List.of(
                text(row.getShoeName()),
                text(row.getColor()),
                text(row.getSize()),
                text(row.getPairsOrdered()),
                text(row.getPairsSold()),
                text(row.getDifference()));
    }

    default List<String> toOrderedNotSellingRow(OrderedNotSellingDTO row)
    {
        return List.of(
                text(row.getShoeName()),
                text(row.getColor()),
                text(row.getSize()),
                text(row.getPairsOrdered()),
                text(row.getPairsSold()),
                text(row.getDaysWithoutSale()));
    }

    default String text(Object value)
    {
        if (Objects.isNull(value))
        {
            return "";
        }
        return String.valueOf(value);
    }

    default String percent(Integer value)
    {
        if (Objects.isNull(value))
        {
            return "";
        }
        return value + "%";
    }

    default String yesNo(Boolean value)
    {
        if (Boolean.TRUE.equals(value))
        {
            return "Sí";
        }
        return "No";
    }

    default String neverSold(Object lastSaleDate)
    {
        if (Objects.isNull(lastSaleDate))
        {
            return "Nunca vendido";
        }
        return String.valueOf(lastSaleDate);
    }
}
