package com.estradahermanos.shoestock.controller;

import com.estradahermanos.shoestock.dto.request.InventoryReportFilterDTO;
import com.estradahermanos.shoestock.dto.request.MerchandiseReportFilterDTO;
import com.estradahermanos.shoestock.dto.request.SalesReportFilterDTO;
import com.estradahermanos.shoestock.dto.request.SummaryReportFilterDTO;
import com.estradahermanos.shoestock.dto.response.ResponseSuccessDTO;
import com.estradahermanos.shoestock.service.ReportInventoryQueryService;
import com.estradahermanos.shoestock.service.ReportMerchandiseQueryService;
import com.estradahermanos.shoestock.service.ReportSalesQueryService;
import com.estradahermanos.shoestock.service.ReportSummaryQueryService;
import com.estradahermanos.shoestock.utilities.ResponseBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Report", description = "Read-only business reports in pairs")
public class ReportController
{
    private final ReportSalesQueryService       reportSalesQueryService;
    private final ReportInventoryQueryService   reportInventoryQueryService;
    private final ReportMerchandiseQueryService reportMerchandiseQueryService;
    private final ReportSummaryQueryService     reportSummaryQueryService;

    @GetMapping("/sales/top-products")
    @Operation(summary = "Best-selling variants, optional period and limit")
    public ResponseEntity<ResponseSuccessDTO> topProducts(
            @RequestParam(value = "start_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "end_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "limit", required = false) Integer limit)
    {
        SalesReportFilterDTO filter = SalesReportFilterDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .limit(limit)
                .build();
        return ResponseBuilder.success(HttpStatus.OK, reportSalesQueryService.topProducts(filter));
    }

    @GetMapping("/sales/bottom-products")
    @Operation(summary = "Least-sold variants, optional period, limit and in-stock filter")
    public ResponseEntity<ResponseSuccessDTO> bottomProducts(
            @RequestParam(value = "start_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "end_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "with_stock_only", required = false) Boolean withStockOnly)
    {
        SalesReportFilterDTO filter = SalesReportFilterDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .limit(limit)
                .withStockOnly(withStockOnly)
                .build();
        return ResponseBuilder.success(HttpStatus.OK, reportSalesQueryService.bottomProducts(filter));
    }

    @GetMapping("/sales/by-period")
    @Operation(summary = "Sales between two dates")
    public ResponseEntity<ResponseSuccessDTO> salesByPeriod(
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam("start_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam("end_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate)
    {
        SalesReportFilterDTO filter = SalesReportFilterDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .build();
        return ResponseBuilder.success(HttpStatus.OK, reportSalesQueryService.byPeriod(filter));
    }

    @GetMapping("/sales/by-category")
    @Operation(summary = "Sales grouped by shoe type")
    public ResponseEntity<ResponseSuccessDTO> salesByCategory(
            @RequestParam(value = "start_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "end_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate)
    {
        SalesReportFilterDTO filter = SalesReportFilterDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .build();
        return ResponseBuilder.success(HttpStatus.OK, reportSalesQueryService.byCategory(filter));
    }

    @GetMapping("/sales/by-size")
    @Operation(summary = "Sales grouped by size")
    public ResponseEntity<ResponseSuccessDTO> salesBySize(
            @RequestParam(value = "start_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "end_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "shoe_code", required = false) String shoeCode)
    {
        SalesReportFilterDTO filter = SalesReportFilterDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .type(type)
                .shoeCode(shoeCode)
                .build();
        return ResponseBuilder.success(HttpStatus.OK, reportSalesQueryService.bySize(filter));
    }

    @GetMapping("/sales/by-supplier")
    @Operation(summary = "Sales grouped by supplier")
    public ResponseEntity<ResponseSuccessDTO> salesBySupplier(
            @RequestParam(value = "start_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "end_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate)
    {
        SalesReportFilterDTO filter = SalesReportFilterDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .build();
        return ResponseBuilder.success(HttpStatus.OK, reportSalesQueryService.bySupplier(filter));
    }

    @GetMapping("/sales/volume")
    @Operation(summary = "Sales volume grouped by day, week or month")
    public ResponseEntity<ResponseSuccessDTO> salesVolume(
            @RequestParam("start_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("end_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam("group_by") String groupBy)
    {
        SalesReportFilterDTO filter = SalesReportFilterDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .groupBy(groupBy)
                .build();
        return ResponseBuilder.success(HttpStatus.OK, reportSalesQueryService.volume(filter));
    }

    @GetMapping("/inventory/slow-movers")
    @Operation(summary = "Variants in stock with no recent sales")
    public ResponseEntity<ResponseSuccessDTO> slowMovers(
            @RequestParam(value = "days", required = false) Integer days,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "supplier_id", required = false) Integer supplierId)
    {
        InventoryReportFilterDTO filter = InventoryReportFilterDTO.builder()
                .days(days)
                .type(type)
                .supplierId(supplierId)
                .build();
        return ResponseBuilder.success(HttpStatus.OK, reportInventoryQueryService.slowMovers(filter));
    }

    @GetMapping("/inventory/low-stock")
    @Operation(summary = "Variants at or below min stock")
    public ResponseEntity<ResponseSuccessDTO> lowStock(
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "supplier_id", required = false) Integer supplierId)
    {
        InventoryReportFilterDTO filter = InventoryReportFilterDTO.builder()
                .type(type)
                .supplierId(supplierId)
                .build();
        return ResponseBuilder.success(HttpStatus.OK, reportInventoryQueryService.lowStock(filter));
    }

    @GetMapping("/inventory/out-of-stock")
    @Operation(summary = "Variants with zero stock")
    public ResponseEntity<ResponseSuccessDTO> outOfStock(
            @RequestParam(value = "start_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "end_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate)
    {
        InventoryReportFilterDTO filter = InventoryReportFilterDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .build();
        return ResponseBuilder.success(HttpStatus.OK, reportInventoryQueryService.outOfStock(filter));
    }

    @GetMapping("/inventory/restock")
    @Operation(summary = "Restock advice from sales and current stock")
    public ResponseEntity<ResponseSuccessDTO> restock(
            @RequestParam(value = "start_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "end_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "supplier_id", required = false) Integer supplierId)
    {
        InventoryReportFilterDTO filter = InventoryReportFilterDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .type(type)
                .supplierId(supplierId)
                .build();
        return ResponseBuilder.success(HttpStatus.OK, reportInventoryQueryService.restock(filter));
    }

    @GetMapping("/inventory/by-group")
    @Operation(summary = "Inventory totals grouped by type or supplier")
    public ResponseEntity<ResponseSuccessDTO> inventoryByGroup(
            @RequestParam("group_by") String groupBy)
    {
        InventoryReportFilterDTO filter = InventoryReportFilterDTO.builder()
                .groupBy(groupBy)
                .build();
        return ResponseBuilder.success(HttpStatus.OK, reportInventoryQueryService.byGroup(filter));
    }

    @GetMapping("/inventory")
    @Operation(summary = "Current stock of every variant")
    public ResponseEntity<ResponseSuccessDTO> inventoryStatus(
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "supplier_id", required = false) Integer supplierId,
            @RequestParam(value = "include_out_of_stock", required = false) Boolean includeOutOfStock)
    {
        InventoryReportFilterDTO filter = InventoryReportFilterDTO.builder()
                .type(type)
                .supplierId(supplierId)
                .includeOutOfStock(includeOutOfStock)
                .build();
        return ResponseBuilder.success(HttpStatus.OK, reportInventoryQueryService.status(filter));
    }

    @GetMapping("/orders/pending")
    @Operation(summary = "Pending merchandise orders")
    public ResponseEntity<ResponseSuccessDTO> pendingOrders(
            @RequestParam(value = "supplier_id", required = false) Integer supplierId)
    {
        MerchandiseReportFilterDTO filter = MerchandiseReportFilterDTO.builder()
                .supplierId(supplierId)
                .build();
        return ResponseBuilder.success(HttpStatus.OK, reportMerchandiseQueryService.pending(filter));
    }

    @GetMapping("/orders/received")
    @Operation(summary = "Received merchandise orders in a date range")
    public ResponseEntity<ResponseSuccessDTO> receivedOrders(
            @RequestParam("start_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("end_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "supplier_id", required = false) Integer supplierId)
    {
        MerchandiseReportFilterDTO filter = MerchandiseReportFilterDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .supplierId(supplierId)
                .build();
        return ResponseBuilder.success(HttpStatus.OK, reportMerchandiseQueryService.received(filter));
    }

    @GetMapping("/orders/by-supplier")
    @Operation(summary = "Orders grouped by supplier")
    public ResponseEntity<ResponseSuccessDTO> ordersBySupplier(
            @RequestParam(value = "start_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "end_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate)
    {
        MerchandiseReportFilterDTO filter = MerchandiseReportFilterDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .build();
        return ResponseBuilder.success(HttpStatus.OK, reportMerchandiseQueryService.bySupplier(filter));
    }

    @GetMapping("/orders/vs-sales")
    @Operation(summary = "Pairs ordered versus pairs sold")
    public ResponseEntity<ResponseSuccessDTO> orderedVsSold(
            @RequestParam("start_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("end_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "supplier_id", required = false) Integer supplierId)
    {
        MerchandiseReportFilterDTO filter = MerchandiseReportFilterDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .supplierId(supplierId)
                .build();
        return ResponseBuilder.success(HttpStatus.OK, reportMerchandiseQueryService.orderedVsSold(filter));
    }

    @GetMapping("/orders/not-selling")
    @Operation(summary = "Ordered variants that are not selling")
    public ResponseEntity<ResponseSuccessDTO> orderedNotSelling(
            @RequestParam(value = "start_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "end_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "days", required = false) Integer days)
    {
        MerchandiseReportFilterDTO filter = MerchandiseReportFilterDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .days(days)
                .build();
        return ResponseBuilder.success(HttpStatus.OK, reportMerchandiseQueryService.orderedNotSelling(filter));
    }

    @GetMapping("/catalog/new-styles")
    @Operation(summary = "Styles created in a date range")
    public ResponseEntity<ResponseSuccessDTO> newStyles(
            @RequestParam("start_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("end_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "supplier_id", required = false) Integer supplierId)
    {
        SummaryReportFilterDTO filter = SummaryReportFilterDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .type(type)
                .supplierId(supplierId)
                .build();
        return ResponseBuilder.success(HttpStatus.OK, reportSummaryQueryService.newStyles(filter));
    }

    @GetMapping("/summary")
    @Operation(summary = "Business snapshot for the period")
    public ResponseEntity<ResponseSuccessDTO> summary(
            @RequestParam(value = "start_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "end_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate)
    {
        SummaryReportFilterDTO filter = SummaryReportFilterDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .build();
        return ResponseBuilder.success(HttpStatus.OK, reportSummaryQueryService.summary(filter));
    }
}
