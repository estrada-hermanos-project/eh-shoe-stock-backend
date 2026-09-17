package com.estradahermanos.shoestock.controller;

import com.estradahermanos.shoestock.dto.request.CreateOrderDetailRequestDTO;
import com.estradahermanos.shoestock.dto.request.CreateOrderRequestDTO;
import com.estradahermanos.shoestock.dto.response.ResponseSuccessDTO;
import com.estradahermanos.shoestock.service.OrderCreateService;
import com.estradahermanos.shoestock.service.OrderDetailCreateService;
import com.estradahermanos.shoestock.service.OrderQueryService;
import com.estradahermanos.shoestock.service.OrderReceiveService;
import com.estradahermanos.shoestock.utilities.ResponseBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "Merchandise orders and reception")
public class OrderController
{
    private final OrderCreateService       orderCreateService;
    private final OrderDetailCreateService orderDetailCreateService;
    private final OrderQueryService        orderQueryService;
    private final OrderReceiveService      orderReceiveService;

    @PostMapping
    @Operation(summary = "Create an order header with status PENDIENTE")
    public ResponseEntity<ResponseSuccessDTO> create(@RequestBody CreateOrderRequestDTO request)
    {
        return ResponseBuilder.success(HttpStatus.CREATED, orderCreateService.create(request));
    }

    @PostMapping("/{orderId}/details")
    @Operation(summary = "Add a detail line to a pending order")
    public ResponseEntity<ResponseSuccessDTO> addDetail(
            @Parameter(description = "Order id") @PathVariable String orderId,
            @RequestBody CreateOrderDetailRequestDTO request)
    {
        return ResponseBuilder.success(HttpStatus.CREATED, orderDetailCreateService.create(orderId, request));
    }

    @GetMapping("/by-status")
    @Operation(summary = "List orders by status")
    public ResponseEntity<ResponseSuccessDTO> findByStatus(
            @Parameter(description = "PENDIENTE or RECIBIDA")
            @RequestParam("status") String status)
    {
        return ResponseBuilder.success(HttpStatus.OK, orderQueryService.findByStatus(status));
    }

    @GetMapping("/supplier/{supplierId}")
    @Operation(summary = "List orders of a supplier ordered by creation date")
    public ResponseEntity<ResponseSuccessDTO> findBySupplier(
            @Parameter(description = "Supplier id") @PathVariable Integer supplierId)
    {
        return ResponseBuilder.success(HttpStatus.OK, orderQueryService.findBySupplier(supplierId));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get a complete order by id")
    public ResponseEntity<ResponseSuccessDTO> findById(
            @Parameter(description = "Order id") @PathVariable String orderId)
    {
        return ResponseBuilder.success(HttpStatus.OK, orderQueryService.findById(orderId));
    }

    @GetMapping
    @Operation(summary = "List orders between two creation dates")
    public ResponseEntity<ResponseSuccessDTO> findByRange(
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam("start_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam("end_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate)
    {
        return ResponseBuilder.success(HttpStatus.OK, orderQueryService.findByDateRange(startDate, endDate));
    }

    @PostMapping("/{orderId}/receive")
    @Operation(summary = "Mark order as RECIBIDA and increase stock of each line")
    public ResponseEntity<ResponseSuccessDTO> receive(
            @Parameter(description = "Order id") @PathVariable String orderId)
    {
        return ResponseBuilder.success(HttpStatus.OK, orderReceiveService.receive(orderId));
    }
}
