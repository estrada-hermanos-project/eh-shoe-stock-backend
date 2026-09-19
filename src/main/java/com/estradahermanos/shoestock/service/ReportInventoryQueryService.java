package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.request.InventoryReportFilterDTO;
import com.estradahermanos.shoestock.dto.response.InventoryGroupDTO;
import com.estradahermanos.shoestock.dto.response.InventoryStatusDTO;
import com.estradahermanos.shoestock.dto.response.LowStockDTO;
import com.estradahermanos.shoestock.dto.response.OutOfStockDTO;
import com.estradahermanos.shoestock.dto.response.RestockSuggestionDTO;
import com.estradahermanos.shoestock.dto.response.SlowMoverDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.mapper.ReportInventoryMapper;
import com.estradahermanos.shoestock.repository.entities.Order;
import com.estradahermanos.shoestock.repository.entities.OrderDetail;
import com.estradahermanos.shoestock.repository.entities.Shoe;
import com.estradahermanos.shoestock.repository.entities.ShoeStock;
import com.estradahermanos.shoestock.repository.projections.LastSaleDateView;
import com.estradahermanos.shoestock.repository.projections.SaleAmountByStockView;
import com.estradahermanos.shoestock.repository.repositories.OrderDetailRepository;
import com.estradahermanos.shoestock.repository.repositories.OrderRepository;
import com.estradahermanos.shoestock.repository.repositories.SaleRepository;
import com.estradahermanos.shoestock.repository.repositories.ShoeStockRepository;
import com.estradahermanos.shoestock.utilities.ExceptionLog;
import com.estradahermanos.shoestock.utilities.OrderStatusEnum;
import com.estradahermanos.shoestock.utilities.ReportCatalogLookup;
import com.estradahermanos.shoestock.utilities.ReportInventoryGroup;
import com.estradahermanos.shoestock.utilities.ReportValidator;
import com.estradahermanos.shoestock.utilities.RestockAdviceEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportInventoryQueryService
{
    private final ShoeStockRepository    shoeStockRepository;
    private final SaleRepository         saleRepository;
    private final OrderRepository        orderRepository;
    private final OrderDetailRepository  orderDetailRepository;
    private final ReportCatalogLookup    catalogLookup;
    private final ReportValidator        reportValidator;
    private final ReportInventoryMapper  reportInventoryMapper;

    /** Current stock of every variant. */
    public List<InventoryStatusDTO> status(InventoryReportFilterDTO filter)
    {
        log.info("Generating inventory status report");
        try
        {
            reportValidator.validateType(filter.getType());
            catalogLookup.requireExistingSupplier(filter.getSupplierId());
            boolean includeOutOfStock = filter.getIncludeOutOfStock() == null || filter.getIncludeOutOfStock();
            return filteredVariants(filter.getType(), filter.getSupplierId()).stream()
                    .filter(variant -> includeOutOfStock || variant.getStock() > 0)
                    .map(variant -> reportInventoryMapper.toStatus(variant, shoeName(variant)))
                    .toList();
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to generate inventory status report", exception);
            throw reportFailed();
        }
    }

    /** Variants with stock that have not sold within the days threshold. */
    public List<SlowMoverDTO> slowMovers(InventoryReportFilterDTO filter)
    {
        log.info("Generating slow movers report");
        try
        {
            reportValidator.validateType(filter.getType());
            catalogLookup.requireExistingSupplier(filter.getSupplierId());
            int days = reportValidator.resolveDays(filter.getDays());
            Map<Integer, LocalDate> lastSales = lastSaleDates();
            Map<Integer, String>    names     = catalogLookup.supplierNames();
            LocalDate today = LocalDate.now();
            List<SlowMoverDTO> result = new ArrayList<>();
            for (ShoeStock variant : filteredVariants(filter.getType(), filter.getSupplierId()))
            {
                if (variant.getStock() <= 0)
                {
                    continue;
                }
                LocalDate lastSale = lastSales.get(variant.getId());
                Long daysWithout   = lastSale == null ? null : ChronoUnit.DAYS.between(lastSale, today);
                boolean slow = lastSale == null || daysWithout >= days;
                if (!slow)
                {
                    continue;
                }
                Shoe shoe = shoes().get(variant.getShoeId());
                result.add(SlowMoverDTO.builder()
                        .shoeStockId(variant.getId())
                        .shoeName(shoe == null ? null : shoe.getName())
                        .color(variant.getColor())
                        .size(variant.getSize())
                        .stock(variant.getStock())
                        .lastSaleDate(lastSale)
                        .daysWithoutSale(daysWithout)
                        .supplierName(shoe == null ? null : names.get(shoe.getSupplier()))
                        .build());
            }
            return result;
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to generate slow movers report", exception);
            throw reportFailed();
        }
    }

    /** Variants at or below min stock. */
    public List<LowStockDTO> lowStock(InventoryReportFilterDTO filter)
    {
        log.info("Generating low stock report");
        try
        {
            reportValidator.validateType(filter.getType());
            catalogLookup.requireExistingSupplier(filter.getSupplierId());
            Map<Integer, String> names = catalogLookup.supplierNames();
            Set<Integer> allowed = filteredVariants(filter.getType(), filter.getSupplierId()).stream()
                    .map(ShoeStock::getId)
                    .collect(Collectors.toSet());
            return shoeStockRepository.findLowStock().stream()
                    .filter(variant -> allowed.contains(variant.getId()))
                    .map(variant ->
                    {
                        int below = Math.max(0, variant.getMinStock() - variant.getStock());
                        Shoe shoe = shoes().get(variant.getShoeId());
                        String supplierName = shoe == null ? null : names.get(shoe.getSupplier());
                        return reportInventoryMapper.toLowStock(
                                variant, shoe == null ? null : shoe.getName(), below, supplierName);
                    })
                    .toList();
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to generate low stock report", exception);
            throw reportFailed();
        }
    }

    /** Variants with zero stock. */
    public List<OutOfStockDTO> outOfStock(InventoryReportFilterDTO filter)
    {
        log.info("Generating out of stock report");
        try
        {
            reportValidator.validateOptionalRange(filter.getStartDate(), filter.getEndDate());
            Map<Integer, Long> sold = soldByStock(filter.getStartDate(), filter.getEndDate());
            Set<Integer> pending = pendingStockIds();
            Map<Integer, String> names = catalogLookup.supplierNames();
            return shoeStockRepository.findByStock(0).stream()
                    .map(variant ->
                    {
                        Shoe shoe = shoes().get(variant.getShoeId());
                        return OutOfStockDTO.builder()
                                .shoeStockId(variant.getId())
                                .shoeName(shoe == null ? null : shoe.getName())
                                .color(variant.getColor())
                                .size(variant.getSize())
                                .type(shoe == null ? null : shoe.getType())
                                .supplierName(shoe == null ? null : names.get(shoe.getSupplier()))
                                .amountSold(sold.getOrDefault(variant.getId(), 0L))
                                .hasPendingOrder(pending.contains(variant.getId()))
                                .build();
                    })
                    .toList();
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to generate out of stock report", exception);
            throw reportFailed();
        }
    }

    /** Restock advice from sales and current stock. */
    public List<RestockSuggestionDTO> restock(InventoryReportFilterDTO filter)
    {
        log.info("Generating restock report");
        try
        {
            reportValidator.validateType(filter.getType());
            catalogLookup.requireExistingSupplier(filter.getSupplierId());
            reportValidator.validateOptionalRange(filter.getStartDate(), filter.getEndDate());
            LocalDate start = filter.getStartDate();
            LocalDate end   = filter.getEndDate();
            if (start == null)
            {
                end   = LocalDate.now();
                start = end.minusDays(ReportValidator.DEFAULT_RESTOCK_DAYS);
            }
            Map<Integer, Long> sold = soldByStock(start, end);
            List<RestockSuggestionDTO> result = new ArrayList<>();
            for (ShoeStock variant : filteredVariants(filter.getType(), filter.getSupplierId()))
            {
                long amountSold = sold.getOrDefault(variant.getId(), 0L);
                result.add(RestockSuggestionDTO.builder()
                        .shoeStockId(variant.getId())
                        .shoeName(shoeName(variant))
                        .color(variant.getColor())
                        .size(variant.getSize())
                        .amountSold(amountSold)
                        .stock(variant.getStock())
                        .minStock(variant.getMinStock())
                        .advice(advice(variant, amountSold))
                        .build());
            }
            result.sort(Comparator.comparingInt(row -> row.getAdvice().ordinal()));
            return result;
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to generate restock report", exception);
            throw reportFailed();
        }
    }

    /** Inventory totals grouped by type or supplier. */
    public List<InventoryGroupDTO> byGroup(InventoryReportFilterDTO filter)
    {
        log.info("Generating inventory by group report");
        try
        {
            ReportInventoryGroup group = reportValidator.parseInventoryGroup(filter.getGroupBy());
            Map<Integer, String> names = catalogLookup.supplierNames();
            Map<String, InventoryGroupDTO> buckets = new LinkedHashMap<>();
            for (ShoeStock variant : catalogLookup.allVariants().values())
            {
                Shoe   shoe  = shoes().get(variant.getShoeId());
                String key   = groupKey(group, shoe, names);
                InventoryGroupDTO bucket = buckets.computeIfAbsent(key, name -> InventoryGroupDTO.builder()
                        .group(name)
                        .variantCount(0L)
                        .pairsInStock(0L)
                        .outOfStockCount(0L)
                        .lowStockCount(0L)
                        .build());
                bucket.setVariantCount(bucket.getVariantCount() + 1);
                bucket.setPairsInStock(bucket.getPairsInStock() + variant.getStock());
                if (variant.getStock() == 0)
                {
                    bucket.setOutOfStockCount(bucket.getOutOfStockCount() + 1);
                }
                if (variant.getStock() <= variant.getMinStock())
                {
                    bucket.setLowStockCount(bucket.getLowStockCount() + 1);
                }
            }
            return new ArrayList<>(buckets.values());
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to generate inventory by group report", exception);
            throw reportFailed();
        }
    }

    private List<ShoeStock> filteredVariants(String type, Integer supplierId)
    {
        return catalogLookup.allVariants().values().stream()
                .filter(variant -> matches(variant, type, supplierId))
                .toList();
    }

    private boolean matches(ShoeStock variant, String type, Integer supplierId)
    {
        Shoe shoe = shoes().get(variant.getShoeId());
        if (type != null && (shoe == null || !type.equals(shoe.getType())))
        {
            return false;
        }
        return supplierId == null || (shoe != null && supplierId.equals(shoe.getSupplier()));
    }

    private String shoeName(ShoeStock variant)
    {
        Shoe shoe = shoes().get(variant.getShoeId());
        return shoe == null ? null : shoe.getName();
    }

    private Map<String, Shoe> shoes()
    {
        return catalogLookup.allShoes();
    }

    private Map<Integer, LocalDate> lastSaleDates()
    {
        return saleRepository.findLastSaleDateGrouped().stream()
                .collect(Collectors.toMap(LastSaleDateView::getShoeStockId, LastSaleDateView::getLastSaleDate));
    }

    private Map<Integer, Long> soldByStock(LocalDate start, LocalDate end)
    {
        return saleRepository.sumAmountGroupedByStock(start, end).stream()
                .collect(Collectors.toMap(SaleAmountByStockView::getShoeStockId, SaleAmountByStockView::getTotalAmount));
    }

    private Set<Integer> pendingStockIds()
    {
        List<Order> pending = orderRepository.findByStatus(OrderStatusEnum.PENDIENTE);
        if (pending.isEmpty())
        {
            return Set.of();
        }
        List<String> ids = pending.stream().map(Order::getId).toList();
        Set<Integer> stockIds = new HashSet<>();
        for (OrderDetail detail : orderDetailRepository.findByOrderIdIn(ids))
        {
            stockIds.add(detail.getShoeStockId());
        }
        return stockIds;
    }

    private RestockAdviceEnum advice(ShoeStock variant, long amountSold)
    {
        if (variant.getStock() == 0 || variant.getStock() <= variant.getMinStock())
        {
            return RestockAdviceEnum.REPONER;
        }
        if (amountSold > variant.getStock())
        {
            return RestockAdviceEnum.VIGILAR;
        }
        return RestockAdviceEnum.NO_PEDIR;
    }

    private String groupKey(ReportInventoryGroup group, Shoe shoe, Map<Integer, String> names)
    {
        if (shoe == null)
        {
            return "Unknown";
        }
        if (group == ReportInventoryGroup.TYPE)
        {
            return shoe.getType();
        }
        return names.getOrDefault(shoe.getSupplier(), "Unknown");
    }

    private BusinessException reportFailed()
    {
        return BusinessException.builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR)
                .message("Could not generate report")
                .build();
    }
}
