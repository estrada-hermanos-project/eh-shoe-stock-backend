package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.request.MerchandiseReportFilterDTO;
import com.estradahermanos.shoestock.dto.response.OrderDetailResponseDTO;
import com.estradahermanos.shoestock.dto.response.OrderedNotSellingDTO;
import com.estradahermanos.shoestock.dto.response.OrderedVsSoldDTO;
import com.estradahermanos.shoestock.dto.response.OrdersBySupplierDTO;
import com.estradahermanos.shoestock.dto.response.PendingOrderReportDTO;
import com.estradahermanos.shoestock.dto.response.ReceivedOrderReportDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.mapper.OrderDetailMapper;
import com.estradahermanos.shoestock.mapper.ReportMerchandiseMapper;
import com.estradahermanos.shoestock.repository.entities.Order;
import com.estradahermanos.shoestock.repository.entities.OrderDetail;
import com.estradahermanos.shoestock.repository.entities.Shoe;
import com.estradahermanos.shoestock.repository.entities.ShoeStock;
import com.estradahermanos.shoestock.repository.entities.Supplier;
import com.estradahermanos.shoestock.repository.projections.LastSaleDateView;
import com.estradahermanos.shoestock.repository.projections.SaleAmountByStockView;
import com.estradahermanos.shoestock.repository.repositories.OrderDetailRepository;
import com.estradahermanos.shoestock.repository.repositories.OrderRepository;
import com.estradahermanos.shoestock.repository.repositories.SaleRepository;
import com.estradahermanos.shoestock.repository.repositories.SupplierRepository;
import com.estradahermanos.shoestock.utilities.ExceptionLog;
import com.estradahermanos.shoestock.utilities.OrderStatusEnum;
import com.estradahermanos.shoestock.utilities.ReportCatalogLookup;
import com.estradahermanos.shoestock.utilities.ReportValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportMerchandiseQueryService
{
    private final OrderRepository          orderRepository;
    private final OrderDetailRepository    orderDetailRepository;
    private final SaleRepository           saleRepository;
    private final SupplierRepository       supplierRepository;
    private final ReportCatalogLookup      catalogLookup;
    private final ReportValidator          reportValidator;
    private final ReportMerchandiseMapper  reportMerchandiseMapper;
    private final OrderDetailMapper        orderDetailMapper;

    /** Pending orders, optional supplier filter. */
    public List<PendingOrderReportDTO> pending(MerchandiseReportFilterDTO filter)
    {
        log.info("Generating pending orders report");
        try
        {
            catalogLookup.requireExistingSupplier(filter.getSupplierId());
            List<Order> orders = filter.getSupplierId() == null
                    ? orderRepository.findByStatus(OrderStatusEnum.PENDIENTE)
                    : orderRepository.findByStatusAndSupplier(OrderStatusEnum.PENDIENTE, filter.getSupplierId());
            return toPending(orders);
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to generate pending orders report", exception);
            throw reportFailed();
        }
    }

    /** Received orders in a required date range. */
    public List<ReceivedOrderReportDTO> received(MerchandiseReportFilterDTO filter)
    {
        log.info("Generating received orders report");
        try
        {
            reportValidator.validateRequiredRange(filter.getStartDate(), filter.getEndDate());
            catalogLookup.requireExistingSupplier(filter.getSupplierId());
            List<Order> orders = filter.getSupplierId() == null
                    ? orderRepository.findByStatusAndDateRange(
                            OrderStatusEnum.RECIBIDA, filter.getStartDate(), filter.getEndDate())
                    : orderRepository.findByStatusAndSupplierAndDateRange(
                            OrderStatusEnum.RECIBIDA, filter.getSupplierId(),
                            filter.getStartDate(), filter.getEndDate());
            return toReceived(orders);
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to generate received orders report", exception);
            throw reportFailed();
        }
    }

    /** Order counts and pairs per supplier. */
    public List<OrdersBySupplierDTO> bySupplier(MerchandiseReportFilterDTO filter)
    {
        log.info("Generating orders by supplier report");
        try
        {
            reportValidator.validateOptionalRange(filter.getStartDate(), filter.getEndDate());
            List<Order> orders = filter.getStartDate() == null
                    ? orderRepository.findAll()
                    : orderRepository.findByOrderDeliveryDateBetween(filter.getStartDate(), filter.getEndDate());
            Map<String, List<OrderDetail>> details = detailsByOrder(orders);
            Map<Integer, OrdersBySupplierDTO> buckets = new HashMap<>();
            for (Supplier supplier : supplierRepository.findAll())
            {
                buckets.put(supplier.getId(), OrdersBySupplierDTO.builder()
                        .supplierId(supplier.getId())
                        .supplierName(supplier.getFullName())
                        .pendingCount(0L)
                        .receivedCount(0L)
                        .pairsRequested(0L)
                        .pairsReceived(0L)
                        .build());
            }
            for (Order order : orders)
            {
                OrdersBySupplierDTO bucket = buckets.get(order.getSupplier());
                if (bucket == null)
                {
                    continue;
                }
                long pairs = details.getOrDefault(order.getId(), List.of()).stream()
                        .mapToLong(OrderDetail::getAmount)
                        .sum();
                bucket.setPairsRequested(bucket.getPairsRequested() + pairs);
                if (order.getStatus() == OrderStatusEnum.PENDIENTE)
                {
                    bucket.setPendingCount(bucket.getPendingCount() + 1);
                }
                else if (order.getStatus() == OrderStatusEnum.RECIBIDA)
                {
                    bucket.setReceivedCount(bucket.getReceivedCount() + 1);
                    bucket.setPairsReceived(bucket.getPairsReceived() + pairs);
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
            ExceptionLog.unexpected("Failed to generate orders by supplier report", exception);
            throw reportFailed();
        }
    }

    /** Pairs ordered versus pairs sold per variant. */
    public List<OrderedVsSoldDTO> orderedVsSold(MerchandiseReportFilterDTO filter)
    {
        log.info("Generating ordered vs sold report");
        try
        {
            reportValidator.validateRequiredRange(filter.getStartDate(), filter.getEndDate());
            catalogLookup.requireExistingSupplier(filter.getSupplierId());
            List<Order> orders = ordersInRange(filter);
            Map<Integer, Long> ordered = pairsByStock(orders);
            Map<Integer, Long> sold    = soldByStock(filter.getStartDate(), filter.getEndDate());
            Map<Integer, ShoeStock> variants = catalogLookup.allVariants();
            Map<String, Shoe> shoes = catalogLookup.allShoes();
            return ordered.keySet().stream()
                    .filter(stockId -> matchesSupplier(variants.get(stockId), shoes, filter.getSupplierId()))
                    .map(stockId ->
                    {
                        ShoeStock variant = variants.get(stockId);
                        Shoe      shoe    = variant == null ? null : shoes.get(variant.getShoeId());
                        long pairsOrdered = ordered.getOrDefault(stockId, 0L);
                        long pairsSold    = sold.getOrDefault(stockId, 0L);
                        return OrderedVsSoldDTO.builder()
                                .shoeStockId(stockId)
                                .shoeName(shoe == null ? null : shoe.getName())
                                .color(variant == null ? null : variant.getColor())
                                .size(variant == null ? null : variant.getSize())
                                .pairsOrdered(pairsOrdered)
                                .pairsSold(pairsSold)
                                .difference(pairsOrdered - pairsSold)
                                .currentStock(variant == null ? 0 : variant.getStock())
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
            ExceptionLog.unexpected("Failed to generate ordered vs sold report", exception);
            throw reportFailed();
        }
    }

    /** Variants that keep being ordered but do not sell. */
    public List<OrderedNotSellingDTO> orderedNotSelling(MerchandiseReportFilterDTO filter)
    {
        log.info("Generating ordered not selling report");
        try
        {
            reportValidator.validateOptionalRange(filter.getStartDate(), filter.getEndDate());
            int days = reportValidator.resolveDays(filter.getDays());
            List<Order> orders = filter.getStartDate() == null
                    ? orderRepository.findAll()
                    : orderRepository.findByOrderDeliveryDateBetween(filter.getStartDate(), filter.getEndDate());
            Map<Integer, Long> ordered = pairsByStock(orders);
            Map<Integer, Long> sold    = soldByStock(filter.getStartDate(), filter.getEndDate());
            Map<Integer, LocalDate> lastSales = lastSaleDates();
            Map<Integer, ShoeStock> variants  = catalogLookup.allVariants();
            Map<String, Shoe> shoes           = catalogLookup.allShoes();
            Map<Integer, String> names        = catalogLookup.supplierNames();
            LocalDate today = LocalDate.now();
            List<OrderedNotSellingDTO> result = new ArrayList<>();
            for (Map.Entry<Integer, Long> entry : ordered.entrySet())
            {
                if (entry.getValue() <= 0)
                {
                    continue;
                }
                long pairsSold = sold.getOrDefault(entry.getKey(), 0L);
                LocalDate lastSale = lastSales.get(entry.getKey());
                Long daysWithout = lastSale == null ? null : ChronoUnit.DAYS.between(lastSale, today);
                boolean notSelling = pairsSold == 0 || (daysWithout != null && daysWithout >= days);
                if (!notSelling)
                {
                    continue;
                }
                ShoeStock variant = variants.get(entry.getKey());
                Shoe      shoe    = variant == null ? null : shoes.get(variant.getShoeId());
                result.add(OrderedNotSellingDTO.builder()
                        .shoeStockId(entry.getKey())
                        .shoeName(shoe == null ? null : shoe.getName())
                        .color(variant == null ? null : variant.getColor())
                        .size(variant == null ? null : variant.getSize())
                        .pairsOrdered(entry.getValue())
                        .pairsSold(pairsSold)
                        .daysWithoutSale(daysWithout)
                        .currentStock(variant == null ? 0 : variant.getStock())
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
            ExceptionLog.unexpected("Failed to generate ordered not selling report", exception);
            throw reportFailed();
        }
    }

    private List<PendingOrderReportDTO> toPending(List<Order> orders)
    {
        Map<String, List<OrderDetail>> details = detailsByOrder(orders);
        Map<Integer, String> names = catalogLookup.supplierNames();
        LocalDate today = LocalDate.now();
        return orders.stream()
                .map(order ->
                {
                    List<OrderDetailResponseDTO> detailDtos = mapDetails(details.getOrDefault(order.getId(), List.of()));
                    int total = detailDtos.stream().mapToInt(OrderDetailResponseDTO::getAmount).sum();
                    return reportMerchandiseMapper.toPending(
                            order,
                            names.get(order.getSupplier()),
                            ChronoUnit.DAYS.between(order.getOrderDeliveryDate(), today),
                            total,
                            detailDtos);
                })
                .toList();
    }

    private List<ReceivedOrderReportDTO> toReceived(List<Order> orders)
    {
        Map<String, List<OrderDetail>> details = detailsByOrder(orders);
        Map<Integer, String> names = catalogLookup.supplierNames();
        return orders.stream()
                .map(order ->
                {
                    List<OrderDetailResponseDTO> detailDtos = mapDetails(details.getOrDefault(order.getId(), List.of()));
                    int total = detailDtos.stream().mapToInt(OrderDetailResponseDTO::getAmount).sum();
                    return reportMerchandiseMapper.toReceived(
                            order, names.get(order.getSupplier()), total, detailDtos);
                })
                .toList();
    }

    private List<OrderDetailResponseDTO> mapDetails(List<OrderDetail> details)
    {
        Map<Integer, ShoeStock> variants = catalogLookup.variantsById(
                details.stream().map(OrderDetail::getShoeStockId).distinct().toList());
        Map<String, Shoe> shoes = catalogLookup.shoesByCode(
                variants.values().stream().map(ShoeStock::getShoeId).distinct().toList());
        return details.stream()
                .map(detail ->
                {
                    ShoeStock variant = variants.get(detail.getShoeStockId());
                    Shoe      shoe    = variant == null ? null : shoes.get(variant.getShoeId());
                    return orderDetailMapper.toResponse(detail, shoe == null ? null : shoe.getName());
                })
                .toList();
    }

    private Map<String, List<OrderDetail>> detailsByOrder(List<Order> orders)
    {
        if (orders.isEmpty())
        {
            return Map.of();
        }
        List<String> ids = orders.stream().map(Order::getId).toList();
        return orderDetailRepository.findByOrderIdIn(ids).stream()
                .collect(Collectors.groupingBy(OrderDetail::getOrderId));
    }

    private List<Order> ordersInRange(MerchandiseReportFilterDTO filter)
    {
        if (filter.getSupplierId() == null)
        {
            return orderRepository.findByOrderDeliveryDateBetween(filter.getStartDate(), filter.getEndDate());
        }
        return orderRepository.findBySupplier(filter.getSupplierId()).stream()
                .filter(order -> !order.getOrderDeliveryDate().isBefore(filter.getStartDate())
                        && !order.getOrderDeliveryDate().isAfter(filter.getEndDate()))
                .toList();
    }

    private Map<Integer, Long> pairsByStock(List<Order> orders)
    {
        Map<Integer, Long> amounts = new HashMap<>();
        for (List<OrderDetail> details : detailsByOrder(orders).values())
        {
            for (OrderDetail detail : details)
            {
                amounts.merge(detail.getShoeStockId(), detail.getAmount().longValue(), Long::sum);
            }
        }
        return amounts;
    }

    private Map<Integer, Long> soldByStock(LocalDate start, LocalDate end)
    {
        return saleRepository.sumAmountGroupedByStock(start, end).stream()
                .collect(Collectors.toMap(SaleAmountByStockView::getShoeStockId, SaleAmountByStockView::getTotalAmount));
    }

    private Map<Integer, LocalDate> lastSaleDates()
    {
        return saleRepository.findLastSaleDateGrouped().stream()
                .collect(Collectors.toMap(LastSaleDateView::getShoeStockId, LastSaleDateView::getLastSaleDate));
    }

    private boolean matchesSupplier(ShoeStock variant, Map<String, Shoe> shoes, Integer supplierId)
    {
        if (supplierId == null)
        {
            return true;
        }
        if (variant == null)
        {
            return false;
        }
        Shoe shoe = shoes.get(variant.getShoeId());
        return shoe != null && supplierId.equals(shoe.getSupplier());
    }

    private BusinessException reportFailed()
    {
        return BusinessException.builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR)
                .message("Could not generate report")
                .build();
    }
}
