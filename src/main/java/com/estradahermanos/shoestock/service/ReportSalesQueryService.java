package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.request.SalesReportFilterDTO;
import com.estradahermanos.shoestock.dto.response.PeriodSaleDTO;
import com.estradahermanos.shoestock.dto.response.ProductSalesRankDTO;
import com.estradahermanos.shoestock.dto.response.SalesByCategoryDTO;
import com.estradahermanos.shoestock.dto.response.SalesBySizeDTO;
import com.estradahermanos.shoestock.dto.response.SalesBySupplierDTO;
import com.estradahermanos.shoestock.dto.response.SalesVolumeDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.mapper.ReportSalesMapper;
import com.estradahermanos.shoestock.repository.entities.Sale;
import com.estradahermanos.shoestock.repository.entities.Shoe;
import com.estradahermanos.shoestock.repository.entities.ShoeStock;
import com.estradahermanos.shoestock.repository.projections.LastSaleDateView;
import com.estradahermanos.shoestock.repository.projections.SaleAmountBySizeView;
import com.estradahermanos.shoestock.repository.projections.SaleAmountByStockView;
import com.estradahermanos.shoestock.repository.projections.SaleAmountBySupplierView;
import com.estradahermanos.shoestock.repository.projections.SaleAmountByTypeView;
import com.estradahermanos.shoestock.repository.repositories.SaleRepository;
import com.estradahermanos.shoestock.repository.repositories.ShoeRepository;
import com.estradahermanos.shoestock.utilities.ExceptionLog;
import com.estradahermanos.shoestock.utilities.ReportCatalogLookup;
import com.estradahermanos.shoestock.utilities.ReportValidator;
import com.estradahermanos.shoestock.utilities.ReportVolumeGroup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportSalesQueryService
{
    private final SaleRepository       saleRepository;
    private final ShoeRepository       shoeRepository;
    private final ReportCatalogLookup  catalogLookup;
    private final ReportValidator      reportValidator;
    private final ReportSalesMapper    reportSalesMapper;

    /** Best-selling variants, optional period and limit. */
    public List<ProductSalesRankDTO> topProducts(SalesReportFilterDTO filter)
    {
        log.info("Generating top products report");
        try
        {
            reportValidator.validateOptionalRange(filter.getStartDate(), filter.getEndDate());
            reportValidator.validateLimit(filter.getLimit());
            List<SaleAmountByStockView> rows = new ArrayList<>(
                    saleRepository.sumAmountGroupedByStock(filter.getStartDate(), filter.getEndDate()));
            rows.sort(Comparator.comparingLong(SaleAmountByStockView::getTotalAmount).reversed());
            return toRankList(applyLimit(rows, filter.getLimit()), false);
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to generate top products report", exception);
            throw reportFailed();
        }
    }

    /** Least-sold variants, optional period, limit and in-stock filter. */
    public List<ProductSalesRankDTO> bottomProducts(SalesReportFilterDTO filter)
    {
        log.info("Generating bottom products report");
        try
        {
            reportValidator.validateOptionalRange(filter.getStartDate(), filter.getEndDate());
            reportValidator.validateLimit(filter.getLimit());
            List<SaleAmountByStockView> rows = new ArrayList<>(
                    saleRepository.sumAmountGroupedByStock(filter.getStartDate(), filter.getEndDate()));
            rows.sort(Comparator.comparingLong(SaleAmountByStockView::getTotalAmount));
            boolean withStockOnly = Boolean.TRUE.equals(filter.getWithStockOnly());
            return toRankList(applyLimit(rows, filter.getLimit()), withStockOnly);
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to generate bottom products report", exception);
            throw reportFailed();
        }
    }

    /** Sales listed between two required dates. */
    public List<PeriodSaleDTO> byPeriod(SalesReportFilterDTO filter)
    {
        log.info("Generating sales by period report");
        try
        {
            reportValidator.validateRequiredRange(filter.getStartDate(), filter.getEndDate());
            List<Sale> sales = saleRepository.findBySaleDateBetween(filter.getStartDate(), filter.getEndDate());
            Map<Integer, ShoeStock> variants = catalogLookup.variantsById(
                    sales.stream().map(Sale::getShoeStockId).distinct().toList());
            Map<String, Shoe> shoes = catalogLookup.shoesByCode(
                    variants.values().stream().map(ShoeStock::getShoeId).distinct().toList());
            return sales.stream()
                    .map(sale ->
                    {
                        ShoeStock variant = variants.get(sale.getShoeStockId());
                        String    name    = variant == null ? null
                                : shoes.get(variant.getShoeId()) == null ? null
                                : shoes.get(variant.getShoeId()).getName();
                        String    color   = variant == null ? null : variant.getColor();
                        return reportSalesMapper.toPeriodSale(sale, name, color);
                    })
                    .toList();
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to generate sales by period report", exception);
            throw reportFailed();
        }
    }

    /** Pairs sold grouped by shoe type. */
    public List<SalesByCategoryDTO> byCategory(SalesReportFilterDTO filter)
    {
        log.info("Generating sales by category report");
        try
        {
            reportValidator.validateOptionalRange(filter.getStartDate(), filter.getEndDate());
            List<SaleAmountByTypeView> rows = saleRepository.sumAmountGroupedByType(
                    filter.getStartDate(), filter.getEndDate());
            long total = rows.stream().mapToLong(SaleAmountByTypeView::getTotalAmount).sum();
            return rows.stream()
                    .map(row -> SalesByCategoryDTO.builder()
                            .type(row.getType())
                            .amountSold(row.getTotalAmount())
                            .saleCount(row.getSaleCount())
                            .sharePercent(sharePercent(row.getTotalAmount(), total))
                            .build())
                    .toList();
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to generate sales by category report", exception);
            throw reportFailed();
        }
    }

    /** Pairs sold grouped by size, optional type and style. */
    public List<SalesBySizeDTO> bySize(SalesReportFilterDTO filter)
    {
        log.info("Generating sales by size report");
        try
        {
            reportValidator.validateOptionalRange(filter.getStartDate(), filter.getEndDate());
            reportValidator.validateType(filter.getType());
            if (filter.getShoeCode() != null && !shoeRepository.existsByCode(filter.getShoeCode()))
            {
                throw BusinessException.builder()
                        .code(HttpStatus.NOT_FOUND)
                        .message("Shoe not found")
                        .build();
            }
            List<SaleAmountBySizeView> rows = saleRepository.sumAmountGroupedBySize(
                    filter.getStartDate(), filter.getEndDate(), filter.getType(), filter.getShoeCode());
            Map<Integer, Integer> stockBySize = stockBySize(filter.getType(), filter.getShoeCode());
            return rows.stream()
                    .map(row -> SalesBySizeDTO.builder()
                            .size(row.getSize())
                            .type(row.getType())
                            .amountSold(row.getTotalAmount())
                            .currentStock(stockBySize.getOrDefault(row.getSize(), 0))
                            .build())
                    .toList();
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to generate sales by size report", exception);
            throw reportFailed();
        }
    }

    /** Pairs sold grouped by artisan. */
    public List<SalesBySupplierDTO> bySupplier(SalesReportFilterDTO filter)
    {
        log.info("Generating sales by supplier report");
        try
        {
            reportValidator.validateOptionalRange(filter.getStartDate(), filter.getEndDate());
            List<SaleAmountBySupplierView> rows = saleRepository.sumAmountGroupedBySupplier(
                    filter.getStartDate(), filter.getEndDate());
            long              total = rows.stream().mapToLong(SaleAmountBySupplierView::getTotalAmount).sum();
            Map<Integer, String> names = catalogLookup.supplierNames();
            return rows.stream()
                    .map(row -> SalesBySupplierDTO.builder()
                            .supplierId(row.getSupplierId())
                            .supplierName(names.get(row.getSupplierId()))
                            .amountSold(row.getTotalAmount())
                            .stylesSold(row.getStylesSold())
                            .sharePercent(sharePercent(row.getTotalAmount(), total))
                            .build())
                    .toList();
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to generate sales by supplier report", exception);
            throw reportFailed();
        }
    }

    /** Sales volume grouped by day, ISO week or month. */
    public List<SalesVolumeDTO> volume(SalesReportFilterDTO filter)
    {
        log.info("Generating sales volume report");
        try
        {
            reportValidator.validateRequiredRange(filter.getStartDate(), filter.getEndDate());
            ReportVolumeGroup group = reportValidator.parseVolumeGroup(filter.getGroupBy());
            List<Sale> sales = saleRepository.findBySaleDateBetween(filter.getStartDate(), filter.getEndDate());
            Map<String, SalesVolumeDTO> grouped = new LinkedHashMap<>();
            for (Sale sale : sales)
            {
                String key = volumeKey(sale.getSaleDate(), group);
                SalesVolumeDTO bucket = grouped.computeIfAbsent(key, period -> SalesVolumeDTO.builder()
                        .period(period)
                        .amountSold(0L)
                        .saleCount(0L)
                        .build());
                bucket.setAmountSold(bucket.getAmountSold() + sale.getAmount());
                bucket.setSaleCount(bucket.getSaleCount() + 1);
            }
            return new ArrayList<>(grouped.values());
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to generate sales volume report", exception);
            throw reportFailed();
        }
    }

    private List<ProductSalesRankDTO> toRankList(List<SaleAmountByStockView> rows, boolean withStockOnly)
    {
        Map<Integer, ShoeStock> variants = catalogLookup.variantsById(
                rows.stream().map(SaleAmountByStockView::getShoeStockId).toList());
        Map<String, Shoe> shoes = catalogLookup.shoesByCode(
                variants.values().stream().map(ShoeStock::getShoeId).distinct().toList());
        Map<Integer, LocalDate> lastSales = saleRepository.findLastSaleDateGrouped().stream()
                .collect(Collectors.toMap(LastSaleDateView::getShoeStockId, LastSaleDateView::getLastSaleDate));

        List<ProductSalesRankDTO> result = new ArrayList<>();
        for (SaleAmountByStockView row : rows)
        {
            ShoeStock variant = variants.get(row.getShoeStockId());
            Integer   stock   = variant == null ? 0 : variant.getStock();
            if (withStockOnly && stock <= 0)
            {
                continue;
            }
            String shoeName = null;
            if (variant != null)
            {
                Shoe shoe = shoes.get(variant.getShoeId());
                shoeName  = shoe == null ? null : shoe.getName();
                result.add(reportSalesMapper.toRank(
                        variant, shoeName, row.getTotalAmount(), stock, lastSales.get(row.getShoeStockId())));
            }
            else
            {
                result.add(ProductSalesRankDTO.builder()
                        .shoeStockId(row.getShoeStockId())
                        .amountSold(row.getTotalAmount())
                        .currentStock(0)
                        .lastSaleDate(lastSales.get(row.getShoeStockId()))
                        .build());
            }
        }
        return result;
    }

    private Map<Integer, Integer> stockBySize(String type, String shoeCode)
    {
        Map<Integer, ShoeStock> variants = catalogLookup.allVariants();
        Map<String, Shoe>       shoes    = catalogLookup.allShoes();
        Map<Integer, Integer>   bySize   = new LinkedHashMap<>();
        for (ShoeStock variant : variants.values())
        {
            Shoe shoe = shoes.get(variant.getShoeId());
            if (type != null && (shoe == null || !type.equals(shoe.getType())))
            {
                continue;
            }
            if (shoeCode != null && !shoeCode.equals(variant.getShoeId()))
            {
                continue;
            }
            bySize.merge(variant.getSize(), variant.getStock(), Integer::sum);
        }
        return bySize;
    }

    private List<SaleAmountByStockView> applyLimit(List<SaleAmountByStockView> rows, Integer limit)
    {
        if (limit == null || rows.size() <= limit)
        {
            return rows;
        }
        return rows.subList(0, limit);
    }

    private String volumeKey(LocalDate date, ReportVolumeGroup group)
    {
        return switch (group)
        {
            case DAY   -> date.toString();
            case WEEK  -> date.get(IsoFields.WEEK_BASED_YEAR) + "-W"
                    + String.format("%02d", date.get(WeekFields.ISO.weekOfWeekBasedYear()));
            case MONTH -> date.getYear() + "-" + String.format("%02d", date.getMonthValue());
        };
    }

    private int sharePercent(Long amount, long total)
    {
        if (total <= 0 || amount == null)
        {
            return 0;
        }
        return (int) Math.round(amount * 100.0 / total);
    }

    private BusinessException reportFailed()
    {
        return BusinessException.builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR)
                .message("Could not generate report")
                .build();
    }
}
