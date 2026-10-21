package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.pdf.ReportPdfDocument;
import com.estradahermanos.shoestock.dto.pdf.ReportPdfSection;
import com.estradahermanos.shoestock.dto.request.SendReportEmailRequestDTO;
import com.estradahermanos.shoestock.dto.response.BusinessSummaryDTO;
import com.estradahermanos.shoestock.dto.response.CatalogSummaryDTO;
import com.estradahermanos.shoestock.dto.response.InventorySummaryDTO;
import com.estradahermanos.shoestock.dto.response.MerchandiseSummaryDTO;
import com.estradahermanos.shoestock.dto.response.ProductSalesRankDTO;
import com.estradahermanos.shoestock.dto.response.SalesSummaryDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.mapper.ReportEmailFilterMapper;
import com.estradahermanos.shoestock.mapper.ReportPdfRowMapper;
import com.estradahermanos.shoestock.utilities.ReportEmailKind;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class ReportPdfDocumentService
{
    private final ReportSalesQueryService reportSalesQueryService;
    private final ReportInventoryQueryService reportInventoryQueryService;
    private final ReportMerchandiseQueryService reportMerchandiseQueryService;
    private final ReportSummaryQueryService reportSummaryQueryService;
    private final ReportEmailFilterMapper reportEmailFilterMapper;
    private final ReportPdfRowMapper reportPdfRowMapper;

    /** Arma el contenido del PDF con los mismos datos y validaciones de cada reporte. */
    public ReportPdfDocument build(SendReportEmailRequestDTO request)
    {
        if (Objects.isNull(request))
        {
            throw BusinessException.builder()
                    .code(HttpStatus.BAD_REQUEST)
                    .message("Request body is required")
                    .build();
        }
        ReportEmailKind kind = ReportEmailKind.of(request.getReport(), request.getSubReport());
        return switch (kind)
        {
            case SUMMARY -> summary(kind, request);
            case SUMMARY_NEW_STYLES -> newStyles(kind, request);
            case SALES_TOP_PRODUCTS -> topProducts(kind, request);
            case SALES_BOTTOM_PRODUCTS -> bottomProducts(kind, request);
            case SALES_BY_PERIOD -> salesByPeriod(kind, request);
            case SALES_BY_CATEGORY -> salesByCategory(kind, request);
            case SALES_BY_SIZE -> salesBySize(kind, request);
            case SALES_BY_SUPPLIER -> salesBySupplier(kind, request);
            case SALES_VOLUME -> salesVolume(kind, request);
            case INVENTORY_STATUS -> inventoryStatus(kind, request);
            case INVENTORY_BY_GROUP -> inventoryByGroup(kind, request);
            case INVENTORY_LOW_STOCK -> lowStock(kind, request);
            case INVENTORY_OUT_OF_STOCK -> outOfStock(kind, request);
            case INVENTORY_SLOW_MOVERS -> slowMovers(kind, request);
            case INVENTORY_RESTOCK -> restock(kind, request);
            case MERCHANDISE_PENDING -> pendingOrders(kind, request);
            case MERCHANDISE_RECEIVED -> receivedOrders(kind, request);
            case MERCHANDISE_BY_SUPPLIER -> ordersBySupplier(kind, request);
            case MERCHANDISE_VS_SALES -> orderedVsSold(kind, request);
            case MERCHANDISE_NOT_SELLING -> orderedNotSelling(kind, request);
        };
    }

    private ReportPdfDocument summary(ReportEmailKind kind, SendReportEmailRequestDTO request)
    {
        BusinessSummaryDTO summary = reportSummaryQueryService.summary(reportEmailFilterMapper.toSummaryFilter(request));
        return document(kind, request, List.of(summarySection(summary), newStylesSection(request)));
    }

    private ReportPdfDocument newStyles(ReportEmailKind kind, SendReportEmailRequestDTO request)
    {
        return document(kind, request, List.of(newStylesSection(request)));
    }

    private ReportPdfSection newStylesSection(SendReportEmailRequestDTO request)
    {
        return section(
                "Estilos registrados en el período",
                List.of("Código", "Nombre", "Tipo", "Proveedor", "Alta"),
                list(reportSummaryQueryService.newStyles(reportEmailFilterMapper.toSummaryFilter(request)))
                        .stream()
                        .map(reportPdfRowMapper::toNewStyleRow)
                        .toList());
    }

    private ReportPdfDocument topProducts(ReportEmailKind kind, SendReportEmailRequestDTO request)
    {
        return rankedSales(kind, request, reportSalesQueryService.topProducts(reportEmailFilterMapper.toSalesFilter(request)));
    }

    private ReportPdfDocument bottomProducts(ReportEmailKind kind, SendReportEmailRequestDTO request)
    {
        return rankedSales(kind, request, reportSalesQueryService.bottomProducts(reportEmailFilterMapper.toSalesFilter(request)));
    }

    private ReportPdfDocument rankedSales(
            ReportEmailKind kind,
            SendReportEmailRequestDTO request,
            List<ProductSalesRankDTO> rows)
    {
        return document(kind, request, List.of(section(
                kind.getTitle(),
                List.of("Código", "Estilo", "Color", "Talla", "Vendidos", "Stock"),
                list(rows).stream().map(reportPdfRowMapper::toTopProductRow).toList())));
    }

    private ReportPdfDocument salesByPeriod(ReportEmailKind kind, SendReportEmailRequestDTO request)
    {
        return document(kind, request, List.of(section(
                kind.getTitle(),
                List.of("Fecha", "Estilo", "Color", "Talla", "Pares"),
                list(reportSalesQueryService.byPeriod(reportEmailFilterMapper.toSalesFilter(request)))
                        .stream()
                        .map(reportPdfRowMapper::toPeriodSaleRow)
                        .toList())));
    }

    private ReportPdfDocument salesByCategory(ReportEmailKind kind, SendReportEmailRequestDTO request)
    {
        return document(kind, request, List.of(section(
                kind.getTitle(),
                List.of("Tipo", "Pares", "Ventas", "%"),
                list(reportSalesQueryService.byCategory(reportEmailFilterMapper.toSalesFilter(request)))
                        .stream()
                        .map(reportPdfRowMapper::toCategoryRow)
                        .toList())));
    }

    private ReportPdfDocument salesBySize(ReportEmailKind kind, SendReportEmailRequestDTO request)
    {
        return document(kind, request, List.of(section(
                kind.getTitle(),
                List.of("Talla", "Tipo", "Pares", "Stock"),
                list(reportSalesQueryService.bySize(reportEmailFilterMapper.toSalesFilter(request)))
                        .stream()
                        .map(reportPdfRowMapper::toSizeRow)
                        .toList())));
    }

    private ReportPdfDocument salesBySupplier(ReportEmailKind kind, SendReportEmailRequestDTO request)
    {
        return document(kind, request, List.of(section(
                kind.getTitle(),
                List.of("Proveedor", "Pares", "Estilos", "%"),
                list(reportSalesQueryService.bySupplier(reportEmailFilterMapper.toSalesFilter(request)))
                        .stream()
                        .map(reportPdfRowMapper::toSalesSupplierRow)
                        .toList())));
    }

    private ReportPdfDocument salesVolume(ReportEmailKind kind, SendReportEmailRequestDTO request)
    {
        return document(kind, request, List.of(section(
                kind.getTitle(),
                List.of("Período", "Pares", "Ventas"),
                list(reportSalesQueryService.volume(reportEmailFilterMapper.toSalesFilter(request)))
                        .stream()
                        .map(reportPdfRowMapper::toVolumeRow)
                        .toList())));
    }

    private ReportPdfDocument inventoryStatus(ReportEmailKind kind, SendReportEmailRequestDTO request)
    {
        return document(kind, request, List.of(section(
                kind.getTitle(),
                List.of("Estilo", "Color", "Talla", "Pares"),
                list(reportInventoryQueryService.status(reportEmailFilterMapper.toInventoryFilter(request)))
                        .stream()
                        .map(reportPdfRowMapper::toInventoryStatusRow)
                        .toList())));
    }

    private ReportPdfDocument inventoryByGroup(ReportEmailKind kind, SendReportEmailRequestDTO request)
    {
        return document(kind, request, List.of(section(
                kind.getTitle(),
                List.of("Grupo", "Variantes", "Pares", "Stock bajo", "Agotados"),
                list(reportInventoryQueryService.byGroup(reportEmailFilterMapper.toInventoryFilter(request)))
                        .stream()
                        .map(reportPdfRowMapper::toInventoryGroupRow)
                        .toList())));
    }

    private ReportPdfDocument lowStock(ReportEmailKind kind, SendReportEmailRequestDTO request)
    {
        return document(kind, request, List.of(section(
                kind.getTitle(),
                List.of("Estilo", "Color", "Talla", "Stock", "Mínimo", "Proveedor"),
                list(reportInventoryQueryService.lowStock(reportEmailFilterMapper.toInventoryFilter(request)))
                        .stream()
                        .map(reportPdfRowMapper::toLowStockRow)
                        .toList())));
    }

    private ReportPdfDocument outOfStock(ReportEmailKind kind, SendReportEmailRequestDTO request)
    {
        return document(kind, request, List.of(section(
                kind.getTitle(),
                List.of("Estilo", "Color", "Talla", "Tipo", "Pedido pendiente"),
                list(reportInventoryQueryService.outOfStock(reportEmailFilterMapper.toInventoryFilter(request)))
                        .stream()
                        .map(reportPdfRowMapper::toOutOfStockRow)
                        .toList())));
    }

    private ReportPdfDocument slowMovers(ReportEmailKind kind, SendReportEmailRequestDTO request)
    {
        return document(kind, request, List.of(section(
                kind.getTitle(),
                List.of("Estilo", "Color", "Talla", "Stock", "Última venta", "Días"),
                list(reportInventoryQueryService.slowMovers(reportEmailFilterMapper.toInventoryFilter(request)))
                        .stream()
                        .map(reportPdfRowMapper::toSlowMoverRow)
                        .toList())));
    }

    private ReportPdfDocument restock(ReportEmailKind kind, SendReportEmailRequestDTO request)
    {
        return document(kind, request, List.of(section(
                kind.getTitle(),
                List.of("Estilo", "Color", "Talla", "Vendidos", "Stock", "Consejo"),
                list(reportInventoryQueryService.restock(reportEmailFilterMapper.toInventoryFilter(request)))
                        .stream()
                        .map(reportPdfRowMapper::toRestockRow)
                        .toList())));
    }

    private ReportPdfDocument pendingOrders(ReportEmailKind kind, SendReportEmailRequestDTO request)
    {
        return document(kind, request, List.of(section(
                kind.getTitle(),
                List.of("Folio", "Proveedor", "Días", "Pares"),
                list(reportMerchandiseQueryService.pending(reportEmailFilterMapper.toMerchandiseFilter(request)))
                        .stream()
                        .map(reportPdfRowMapper::toPendingOrderRow)
                        .toList())));
    }

    private ReportPdfDocument receivedOrders(ReportEmailKind kind, SendReportEmailRequestDTO request)
    {
        return document(kind, request, List.of(section(
                kind.getTitle(),
                List.of("Folio", "Proveedor", "Fecha", "Pares"),
                list(reportMerchandiseQueryService.received(reportEmailFilterMapper.toMerchandiseFilter(request)))
                        .stream()
                        .map(reportPdfRowMapper::toReceivedOrderRow)
                        .toList())));
    }

    private ReportPdfDocument ordersBySupplier(ReportEmailKind kind, SendReportEmailRequestDTO request)
    {
        return document(kind, request, List.of(section(
                kind.getTitle(),
                List.of("Proveedor", "Pendientes", "Recibidos", "Pares pedidos", "Pares recibidos"),
                list(reportMerchandiseQueryService.bySupplier(reportEmailFilterMapper.toMerchandiseFilter(request)))
                        .stream()
                        .map(reportPdfRowMapper::toOrdersSupplierRow)
                        .toList())));
    }

    private ReportPdfDocument orderedVsSold(ReportEmailKind kind, SendReportEmailRequestDTO request)
    {
        return document(kind, request, List.of(section(
                kind.getTitle(),
                List.of("Estilo", "Color", "Talla", "Pedidos", "Vendidos", "Diferencia"),
                list(reportMerchandiseQueryService.orderedVsSold(reportEmailFilterMapper.toMerchandiseFilter(request)))
                        .stream()
                        .map(reportPdfRowMapper::toOrderedVsSoldRow)
                        .toList())));
    }

    private ReportPdfDocument orderedNotSelling(ReportEmailKind kind, SendReportEmailRequestDTO request)
    {
        return document(kind, request, List.of(section(
                kind.getTitle(),
                List.of("Estilo", "Color", "Talla", "Pedidos", "Vendidos", "Días sin venta"),
                list(reportMerchandiseQueryService.orderedNotSelling(reportEmailFilterMapper.toMerchandiseFilter(request)))
                        .stream()
                        .map(reportPdfRowMapper::toOrderedNotSellingRow)
                        .toList())));
    }

    private ReportPdfSection summarySection(BusinessSummaryDTO summary)
    {
        SalesSummaryDTO sales = Optional.ofNullable(summary).map(BusinessSummaryDTO::getSales).orElse(null);
        InventorySummaryDTO inventory = Optional.ofNullable(summary).map(BusinessSummaryDTO::getInventory).orElse(null);
        MerchandiseSummaryDTO merchandise = Optional.ofNullable(summary).map(BusinessSummaryDTO::getMerchandise).orElse(null);
        CatalogSummaryDTO catalog = Optional.ofNullable(summary).map(BusinessSummaryDTO::getCatalog).orElse(null);
        return section("Indicadores", List.of("Indicador", "Valor"), List.of(
                reportPdfRowMapper.toMetricRow("Pares vendidos", value(sales, SalesSummaryDTO::getAmountSold)),
                reportPdfRowMapper.toMetricRow("Ventas", value(sales, SalesSummaryDTO::getSaleCount)),
                reportPdfRowMapper.toMetricRow("Pares en stock", value(inventory, InventorySummaryDTO::getPairsInStock)),
                reportPdfRowMapper.toMetricRow("Stock bajo", value(inventory, InventorySummaryDTO::getLowStockCount)),
                reportPdfRowMapper.toMetricRow("Agotados", value(inventory, InventorySummaryDTO::getOutOfStockCount)),
                reportPdfRowMapper.toMetricRow("Pedidos pendientes", value(merchandise, MerchandiseSummaryDTO::getPendingOrders)),
                reportPdfRowMapper.toMetricRow("Pares recibidos", value(merchandise, MerchandiseSummaryDTO::getPairsReceived)),
                reportPdfRowMapper.toMetricRow("Estilos nuevos", value(catalog, CatalogSummaryDTO::getNewStyles))));
    }

    private <T> Object value(T source, Function<T, Object> getter)
    {
        if (Objects.isNull(source))
        {
            return "";
        }
        return getter.apply(source);
    }

    private ReportPdfDocument document(ReportEmailKind kind, SendReportEmailRequestDTO request, List<ReportPdfSection> sections)
    {
        return ReportPdfDocument.builder()
                .title(kind.getTitle())
                .subtitle(subtitle(request))
                .fileName(kind.getFileName())
                .sections(sections)
                .build();
    }

    private ReportPdfSection section(String heading, List<String> headers, List<List<String>> rows)
    {
        return ReportPdfSection.builder()
                .heading(heading)
                .headers(headers)
                .rows(rows)
                .build();
    }

    private String subtitle(SendReportEmailRequestDTO request)
    {
        List<String> parts = new ArrayList<>();
        if (!Objects.isNull(request.getStartDate()) && !Objects.isNull(request.getEndDate()))
        {
            parts.add("Período " + request.getStartDate() + " a " + request.getEndDate());
        }
        if (StringUtils.hasText(request.getType()))
        {
            parts.add("Tipo: " + request.getType().trim());
        }
        if (!Objects.isNull(request.getSupplierId()))
        {
            parts.add("Proveedor: " + request.getSupplierId());
        }
        if (StringUtils.hasText(request.getGroupBy()))
        {
            parts.add("Agrupado por: " + request.getGroupBy().trim());
        }
        if (parts.isEmpty())
        {
            return "Estrada Hermanos";
        }
        return String.join(" · ", parts);
    }

    private <T> List<T> list(List<T> values)
    {
        if (Objects.isNull(values))
        {
            return List.of();
        }
        return values;
    }
}
