package com.estradahermanos.shoestock.controller;

import com.estradahermanos.shoestock.dto.request.CreateShoeRequestDTO;
import com.estradahermanos.shoestock.dto.response.ResponseSuccessDTO;
import com.estradahermanos.shoestock.service.ShoeCreateService;
import com.estradahermanos.shoestock.service.ShoeDeleteService;
import com.estradahermanos.shoestock.service.ShoeQueryService;
import com.estradahermanos.shoestock.utilities.ResponseBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shoes")
@RequiredArgsConstructor
@Tag(name = "Shoe", description = "Shoe catalog (styles)")
public class ShoeController
{
    private final ShoeCreateService shoeCreateService;
    private final ShoeQueryService  shoeQueryService;
    private final ShoeDeleteService shoeDeleteService;

    @PostMapping
    @Operation(summary = "Register a new shoe style")
    public ResponseEntity<ResponseSuccessDTO> create(@RequestBody CreateShoeRequestDTO request)
    {
        return ResponseBuilder.success(HttpStatus.CREATED, shoeCreateService.create(request));
    }

    @GetMapping("/{code}")
    @Operation(summary = "Get a shoe style by code")
    public ResponseEntity<ResponseSuccessDTO> findByCode(
            @Parameter(description = "Unique shoe code") @PathVariable String code)
    {
        return ResponseBuilder.success(HttpStatus.OK, shoeQueryService.findByCode(code));
    }

    @GetMapping
    @Operation(summary = "List all shoe styles")
    public ResponseEntity<ResponseSuccessDTO> findAll()
    {
        return ResponseBuilder.success(HttpStatus.OK, shoeQueryService.findAll());
    }

    @GetMapping("/supplier/{supplierId}")
    @Operation(summary = "List shoe styles by supplier")
    public ResponseEntity<ResponseSuccessDTO> findBySupplier(
            @Parameter(description = "Supplier id") @PathVariable Integer supplierId)
    {
        return ResponseBuilder.success(HttpStatus.OK, shoeQueryService.findBySupplier(supplierId));
    }

    @DeleteMapping("/{code}")
    @Operation(summary = "Delete a shoe style by code")
    public ResponseEntity<ResponseSuccessDTO> delete(
            @Parameter(description = "Unique shoe code") @PathVariable String code)
    {
        shoeDeleteService.deleteByCode(code);
        return ResponseBuilder.success(HttpStatus.OK, null);
    }
}
