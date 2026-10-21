package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.pdf.ReportPdfDocument;
import com.estradahermanos.shoestock.dto.request.SendReportEmailRequestDTO;
import com.estradahermanos.shoestock.dto.response.BusinessSummaryDTO;
import com.estradahermanos.shoestock.dto.response.CatalogSummaryDTO;
import com.estradahermanos.shoestock.dto.response.InventoryGroupDTO;
import com.estradahermanos.shoestock.dto.response.InventoryStatusDTO;
import com.estradahermanos.shoestock.dto.response.InventorySummaryDTO;
import com.estradahermanos.shoestock.dto.response.LowStockDTO;
import com.estradahermanos.shoestock.dto.response.MerchandiseSummaryDTO;
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
import com.estradahermanos.shoestock.dto.response.SalesSummaryDTO;
import com.estradahermanos.shoestock.dto.response.SalesVolumeDTO;
import com.estradahermanos.shoestock.dto.response.SlowMoverDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.mapper.ReportEmailFilterMapper;
import com.estradahermanos.shoestock.mapper.ReportPdfRowMapper;
import com.estradahermanos.shoestock.utilities.RestockAdviceEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportPdfDocumentServiceTest
{
    @Mock
    private ReportSalesQueryService reportSalesQueryService;

    @Mock
    private ReportInventoryQueryService reportInventoryQueryService;

    @Mock
    private ReportMerchandiseQueryService reportMerchandiseQueryService;

    @Mock
    private ReportSummaryQueryService reportSummaryQueryService;

    private ReportPdfDocumentService service;

    @BeforeEach
    void setUp()
    {
        service = new ReportPdfDocumentService(
                reportSalesQueryService,
                reportInventoryQueryService,
                reportMerchandiseQueryService,
                reportSummaryQueryService,
                Mappers.getMapper(ReportEmailFilterMapper.class),
                Mappers.getMapper(ReportPdfRowMapper.class));
    }

    @Test
    void rejectsNullRequest()
    {
        BusinessException exception = assertThrows(BusinessException.class, () -> service.build(null));
        assertEquals("Request body is required", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
    }

    @Test
    void rejectsUnknownReport()
    {
        assertEquals("Unknown report", assertThrows(BusinessException.class, () -> service.build(request("catalog", null)))
                .getMessage());
    }

    @Test
    void buildsSummaryWithNewStyles()
    {
        when(reportSummaryQueryService.summary(any())).thenReturn(BusinessSummaryDTO.builder()
                .sales(SalesSummaryDTO.builder().amountSold(18L).saleCount(5L).build())
                .inventory(InventorySummaryDTO.builder().pairsInStock(40L).lowStockCount(2L).outOfStockCount(1L).build())
                .merchandise(MerchandiseSummaryDTO.builder().pendingOrders(3L).pairsReceived(20L).build())
                .catalog(CatalogSummaryDTO.builder().newStyles(1L).build())
                .build());
        when(reportSummaryQueryService.newStyles(any())).thenReturn(List.of(NewStyleDTO.builder()
                .code("OXF-001")
                .name("Oxford")
                .type("Caballero")
                .supplierName("Maria")
                .createdAt(LocalDate.of(2026, 9, 2))
                .build()));

        ReportPdfDocument document = service.build(request("summary", null));

        assertEquals("Resumen", document.getTitle());
        assertEquals("reporte-resumen.pdf", document.getFileName());
        assertEquals(2, document.getSections().size());
        assertEquals("18", document.getSections().get(0).getRows().get(0).get(1));
        assertEquals("OXF-001", document.getSections().get(1).getRows().get(0).get(0));
    }

    @Test
    void buildsSummaryWhenNestedDataIsMissing()
    {
        when(reportSummaryQueryService.summary(any())).thenReturn(null);
        when(reportSummaryQueryService.newStyles(any())).thenReturn(null);

        ReportPdfDocument summary = service.build(request("summary", null));
        ReportPdfDocument styles = service.build(request("summary", "new-styles"));

        assertEquals("", summary.getSections().get(0).getRows().get(0).get(1));
        assertEquals("Resumen · Estilos nuevos", styles.getTitle());
        assertTrue(styles.getSections().get(0).getRows().isEmpty());
    }

    @Test
    void buildsEverySalesInventoryAndMerchandiseReport()
    {
        when(reportSalesQueryService.topProducts(any())).thenReturn(List.of(ProductSalesRankDTO.builder()
                .shoeCode("OXF-001").shoeName("Oxford").color("Negro").size(42).amountSold(8L).currentStock(4).build()));
        when(reportSalesQueryService.bottomProducts(any())).thenReturn(List.of());
        when(reportSalesQueryService.byPeriod(any())).thenReturn(List.of(PeriodSaleDTO.builder()
                .saleDate(LocalDate.of(2026, 9, 2)).shoeName("Oxford").color("Negro").size(42).amount(2).build()));
        when(reportSalesQueryService.byCategory(any())).thenReturn(List.of(SalesByCategoryDTO.builder()
                .type("Dama").amountSold(10L).saleCount(3L).sharePercent(40).build()));
        when(reportSalesQueryService.bySize(any())).thenReturn(List.of(SalesBySizeDTO.builder()
                .size(38).type("Dama").amountSold(4L).currentStock(2).build()));
        when(reportSalesQueryService.bySupplier(any())).thenReturn(List.of(SalesBySupplierDTO.builder()
                .supplierName("Maria").amountSold(6L).stylesSold(2L).sharePercent(50).build()));
        when(reportSalesQueryService.volume(any())).thenReturn(List.of(SalesVolumeDTO.builder()
                .period("2026-09-02").amountSold(3L).saleCount(1L).build()));
        when(reportInventoryQueryService.status(any())).thenReturn(List.of(InventoryStatusDTO.builder()
                .shoeName("Oxford").color("Negro").size(42).stock(4).build()));
        when(reportInventoryQueryService.byGroup(any())).thenReturn(List.of(InventoryGroupDTO.builder()
                .group("Dama").variantCount(3L).pairsInStock(12L).lowStockCount(1L).outOfStockCount(0L).build()));
        when(reportInventoryQueryService.lowStock(any())).thenReturn(List.of(LowStockDTO.builder()
                .shoeName("Oxford").color("Negro").size(42).stock(1).minStock(4).supplierName("Maria").build()));
        when(reportInventoryQueryService.outOfStock(any())).thenReturn(List.of(OutOfStockDTO.builder()
                .shoeName("Oxford").color("Negro").size(42).type("Caballero").hasPendingOrder(true).build()));
        when(reportInventoryQueryService.slowMovers(any())).thenReturn(List.of(SlowMoverDTO.builder()
                .shoeName("Oxford").color("Negro").size(42).stock(3).daysWithoutSale(50L).build()));
        when(reportInventoryQueryService.restock(any())).thenReturn(List.of(RestockSuggestionDTO.builder()
                .shoeName("Oxford").color("Negro").size(42).amountSold(8L).stock(1).advice(RestockAdviceEnum.REPONER).build()));
        when(reportMerchandiseQueryService.pending(any())).thenReturn(List.of(PendingOrderReportDTO.builder()
                .orderId("ORDEN-1").supplierName("Maria").daysPending(4L).totalPairs(6).build()));
        when(reportMerchandiseQueryService.received(any())).thenReturn(List.of(ReceivedOrderReportDTO.builder()
                .orderId("ORDEN-2").supplierName("Maria").creationDate(LocalDate.of(2026, 9, 10)).totalPairs(8).build()));
        when(reportMerchandiseQueryService.bySupplier(any())).thenReturn(List.of(OrdersBySupplierDTO.builder()
                .supplierName("Maria").pendingCount(1L).receivedCount(2L).pairsRequested(10L).pairsReceived(8L).build()));
        when(reportMerchandiseQueryService.orderedVsSold(any())).thenReturn(List.of(OrderedVsSoldDTO.builder()
                .shoeName("Oxford").color("Negro").size(42).pairsOrdered(10L).pairsSold(4L).difference(6L).build()));
        when(reportMerchandiseQueryService.orderedNotSelling(any())).thenReturn(List.of(OrderedNotSellingDTO.builder()
                .shoeName("Oxford").color("Negro").size(42).pairsOrdered(6L).pairsSold(0L).daysWithoutSale(20L).build()));

        assertEquals("OXF-001", firstCell(service.build(request("sales", "top-products"))));
        assertTrue(service.build(request("sales", "bottom-products")).getSections().get(0).getRows().isEmpty());
        assertEquals("2026-09-02", firstCell(service.build(request("sales", "by-period"))));
        assertEquals("Dama", firstCell(service.build(request("sales", "by-category"))));
        assertEquals("38", firstCell(service.build(request("sales", "by-size"))));
        assertEquals("Maria", firstCell(service.build(request("sales", "by-supplier"))));
        assertEquals("2026-09-02", firstCell(service.build(request("sales", "volume"))));
        assertEquals("Oxford", firstCell(service.build(request("inventory", "status"))));
        assertEquals("Dama", firstCell(service.build(request("inventory", "by-group"))));
        assertEquals("Oxford", firstCell(service.build(request("inventory", "low-stock"))));
        assertEquals("Sí", service.build(request("inventory", "out-of-stock")).getSections().get(0).getRows().get(0).get(4));
        assertEquals("Nunca vendido", service.build(request("inventory", "slow-movers")).getSections().get(0).getRows().get(0).get(4));
        assertEquals("REPONER", service.build(request("inventory", "restock")).getSections().get(0).getRows().get(0).get(5));
        assertEquals("ORDEN-1", firstCell(service.build(request("merchandise", "pending"))));
        assertEquals("ORDEN-2", firstCell(service.build(request("merchandise", "received"))));
        assertEquals("Maria", firstCell(service.build(request("merchandise", "by-supplier"))));
        assertEquals("6", service.build(request("merchandise", "vs-sales")).getSections().get(0).getRows().get(0).get(5));
        assertEquals("20", service.build(request("merchandise", "not-selling")).getSections().get(0).getRows().get(0).get(5));
    }

    @Test
    void buildsSubtitleFromFilters()
    {
        when(reportInventoryQueryService.status(any())).thenReturn(List.of());

        ReportPdfDocument document = service.build(SendReportEmailRequestDTO.builder()
                .report("inventory")
                .subReport("status")
                .startDate(LocalDate.of(2026, 9, 1))
                .endDate(LocalDate.of(2026, 9, 30))
                .type("Dama")
                .supplierId(2)
                .groupBy("TYPE")
                .build());

        assertTrue(document.getSubtitle().contains("2026-09-01"));
        assertTrue(document.getSubtitle().contains("Dama"));
        assertTrue(document.getSubtitle().contains("2"));
        assertTrue(document.getSubtitle().contains("TYPE"));
    }

    @Test
    void usesDefaultSubtitleWhenNoFilters()
    {
        when(reportInventoryQueryService.status(any())).thenReturn(List.of());
        assertEquals("Estrada Hermanos", service.build(request("inventory", "status")).getSubtitle());
    }

    @Test
    void propagatesReportValidation()
    {
        when(reportSalesQueryService.byPeriod(any())).thenThrow(BusinessException.builder()
                .code(HttpStatus.BAD_REQUEST)
                .message("start_date must not be after end_date")
                .build());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.build(request("sales", "by-period")));
        assertEquals("start_date must not be after end_date", exception.getMessage());
    }

    private String firstCell(ReportPdfDocument document)
    {
        return document.getSections().get(0).getRows().get(0).get(0);
    }

    private SendReportEmailRequestDTO request(String report, String subReport)
    {
        return SendReportEmailRequestDTO.builder()
                .report(report)
                .subReport(subReport)
                .build();
    }
}
