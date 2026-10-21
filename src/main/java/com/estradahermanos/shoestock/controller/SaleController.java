package com.estradahermanos.shoestock.controller;

import com.estradahermanos.shoestock.dto.request.CreateSaleRequestDTO;
import com.estradahermanos.shoestock.dto.response.ResponseSuccessDTO;
import com.estradahermanos.shoestock.service.SaleQueryService;
import com.estradahermanos.shoestock.service.SaleRegisterService;
import com.estradahermanos.shoestock.utilities.ResponseBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
@Tag(name = "Sale", description = "Sales registration and history")
public class SaleController
{
    private final SaleRegisterService saleRegisterService;
    private final SaleQueryService    saleQueryService;

    @PostMapping
    @Operation(summary = "Register a sale, decrease stock and email administrators when the variant is low or sold out")
    public ResponseEntity<ResponseSuccessDTO> register(@RequestBody CreateSaleRequestDTO request)
    {
        return ResponseBuilder.success(HttpStatus.CREATED, saleRegisterService.register(request));
    }

    @GetMapping
    @Operation(summary = "Sales history between two dates, optionally filtered by style code")
    public ResponseEntity<ResponseSuccessDTO> findByRange(
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam("start_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam("end_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Style code (shoe.code). Omit to include every style")
            @RequestParam(value = "shoe_code", required = false) String shoeCode)
    {
        return ResponseBuilder.success(HttpStatus.OK, saleQueryService.findByDateRange(startDate, endDate, shoeCode));
    }

    @GetMapping("/by-date")
    @Operation(summary = "Sales history for a specific date, optionally filtered by style code")
    public ResponseEntity<ResponseSuccessDTO> findByDate(
            @Parameter(description = "Date (YYYY-MM-DD)")
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "Style code (shoe.code). Omit to include every style")
            @RequestParam(value = "shoe_code", required = false) String shoeCode)
    {
        return ResponseBuilder.success(HttpStatus.OK, saleQueryService.findByDate(date, shoeCode));
    }
}
