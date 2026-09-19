package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.request.InventoryReportFilterDTO;
import com.estradahermanos.shoestock.dto.request.SalesReportFilterDTO;
import com.estradahermanos.shoestock.dto.request.SummaryReportFilterDTO;
import com.estradahermanos.shoestock.dto.response.BusinessSummaryDTO;
import com.estradahermanos.shoestock.dto.response.CatalogSummaryDTO;
import com.estradahermanos.shoestock.dto.response.InventorySummaryDTO;
import com.estradahermanos.shoestock.dto.response.MerchandiseSummaryDTO;
import com.estradahermanos.shoestock.dto.response.NewStyleDTO;
import com.estradahermanos.shoestock.dto.response.ProductSalesRankDTO;
import com.estradahermanos.shoestock.dto.response.SalesSummaryDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.repository.entities.Order;
import com.estradahermanos.shoestock.repository.entities.OrderDetail;
import com.estradahermanos.shoestock.repository.entities.ShoeStock;
import com.estradahermanos.shoestock.repository.projections.SaleAmountByStockView;
import com.estradahermanos.shoestock.repository.repositories.OrderDetailRepository;
import com.estradahermanos.shoestock.repository.repositories.OrderRepository;
import com.estradahermanos.shoestock.repository.repositories.SaleRepository;
import com.estradahermanos.shoestock.repository.repositories.ShoeRepository;
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
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportSummaryQueryService
{
    private final ReportSalesQueryService     reportSalesQueryService;
    private final ReportInventoryQueryService reportInventoryQueryService;
    private final SaleRepository              saleRepository;
    private final OrderRepository             orderRepository;
    private final OrderDetailRepository       orderDetailRepository;
    private final ShoeRepository              shoeRepository;
    private final ReportCatalogLookup         catalogLookup;
    private final ReportValidator             reportValidator;

    /** Business snapshot for the period (defaults to the current month). */
    public BusinessSummaryDTO summary(SummaryReportFilterDTO filter)
    {
        log.info("Generating business summary report");
        try
        {
            reportValidator.validateOptionalRange(filter.getStartDate(), filter.getEndDate());
            LocalDate start = filter.getStartDate();
            LocalDate end   = filter.getEndDate();
            if (start == null)
            {
                LocalDate today = LocalDate.now();
                start = today.withDayOfMonth(1);
                end   = today;
            }
            return BusinessSummaryDTO.builder()
                    .sales(salesSummary(start, end))
                    .inventory(inventorySummary())
                    .merchandise(merchandiseSummary(start, end))
                    .catalog(catalogSummary(start, end))
                    .build();
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to generate business summary report", exception);
            throw reportFailed();
        }
    }

    /** Styles created in the required period. */
    public List<NewStyleDTO> newStyles(SummaryReportFilterDTO filter)
    {
        log.info("Generating new styles report");
        try
        {
            reportValidator.validateRequiredRange(filter.getStartDate(), filter.getEndDate());
            reportValidator.validateType(filter.getType());
            catalogLookup.requireExistingSupplier(filter.getSupplierId());
            Map<Integer, String> names = catalogLookup.supplierNames();
            return shoeRepository.findByCreatedAtBetween(filter.getStartDate(), filter.getEndDate()).stream()
                    .filter(shoe -> filter.getType() == null || filter.getType().equals(shoe.getType()))
                    .filter(shoe -> filter.getSupplierId() == null || filter.getSupplierId().equals(shoe.getSupplier()))
                    .map(shoe -> NewStyleDTO.builder()
                            .code(shoe.getCode())
                            .name(shoe.getName())
                            .type(shoe.getType())
                            .supplierName(names.get(shoe.getSupplier()))
                            .description(shoe.getDescription())
                            .createdAt(shoe.getCreatedAt())
                            .build())
                    .toList();
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to generate new styles report", exception);
            throw reportFailed();
        }
    }

    private SalesSummaryDTO salesSummary(LocalDate start, LocalDate end)
    {
        List<SaleAmountByStockView> rows = saleRepository.sumAmountGroupedByStock(start, end);
        long amountSold = rows.stream().mapToLong(SaleAmountByStockView::getTotalAmount).sum();
        long saleCount  = saleRepository.findBySaleDateBetween(start, end).size();
        List<ProductSalesRankDTO> top = reportSalesQueryService.topProducts(SalesReportFilterDTO.builder()
                .startDate(start)
                .endDate(end)
                .limit(1)
                .build());
        return SalesSummaryDTO.builder()
                .amountSold(amountSold)
                .saleCount(saleCount)
                .topProduct(top.isEmpty() ? null : top.get(0))
                .build();
    }

    private InventorySummaryDTO inventorySummary()
    {
        List<ShoeStock> variants = catalogLookup.allVariants().values().stream().toList();
        long pairs = variants.stream().mapToLong(ShoeStock::getStock).sum();
        long low   = variants.stream().filter(v -> v.getStock() <= v.getMinStock()).count();
        long out   = variants.stream().filter(v -> v.getStock() == 0).count();
        long slow  = reportInventoryQueryService.slowMovers(InventoryReportFilterDTO.builder().build()).size();
        return InventorySummaryDTO.builder()
                .pairsInStock(pairs)
                .lowStockCount(low)
                .outOfStockCount(out)
                .slowMoverCount(slow)
                .build();
    }

    private MerchandiseSummaryDTO merchandiseSummary(LocalDate start, LocalDate end)
    {
        List<Order> pending  = orderRepository.findByStatus(OrderStatusEnum.PENDIENTE);
        List<Order> received = orderRepository.findByStatusAndDateRange(OrderStatusEnum.RECIBIDA, start, end);
        return MerchandiseSummaryDTO.builder()
                .pendingOrders((long) pending.size())
                .pairsPending(sumPairs(pending))
                .pairsReceived(sumPairs(received))
                .build();
    }

    private CatalogSummaryDTO catalogSummary(LocalDate start, LocalDate end)
    {
        long styles    = shoeRepository.findAll().size();
        long newStyles = shoeRepository.findByCreatedAtBetween(start, end).size();
        return CatalogSummaryDTO.builder()
                .styleCount(styles)
                .newStyles(newStyles)
                .build();
    }

    private long sumPairs(List<Order> orders)
    {
        if (orders.isEmpty())
        {
            return 0L;
        }
        List<String> ids = orders.stream().map(Order::getId).toList();
        return orderDetailRepository.findByOrderIdIn(ids).stream()
                .mapToLong(OrderDetail::getAmount)
                .sum();
    }

    private BusinessException reportFailed()
    {
        return BusinessException.builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR)
                .message("Could not generate report")
                .build();
    }
}
