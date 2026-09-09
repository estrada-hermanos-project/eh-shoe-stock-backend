package com.estradahermanos.shoestock.controller;

import com.estradahermanos.shoestock.dto.request.CreateSupplierRequestDTO;
import com.estradahermanos.shoestock.dto.request.UpdateSupplierRequestDTO;
import com.estradahermanos.shoestock.dto.response.ResponseSuccessDTO;
import com.estradahermanos.shoestock.service.SupplierCreateService;
import com.estradahermanos.shoestock.service.SupplierDeleteService;
import com.estradahermanos.shoestock.service.SupplierQueryService;
import com.estradahermanos.shoestock.service.SupplierUpdateService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
@Tag(name = "Supplier", description = "Supplier master data")
public class SupplierController
{
    private final SupplierCreateService supplierCreateService;
    private final SupplierQueryService  supplierQueryService;
    private final SupplierUpdateService supplierUpdateService;
    private final SupplierDeleteService supplierDeleteService;

    @PostMapping
    @Operation(summary = "Register a new supplier")
    public ResponseEntity<ResponseSuccessDTO> create(@RequestBody CreateSupplierRequestDTO request)
    {
        return ResponseBuilder.success(HttpStatus.CREATED, supplierCreateService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a supplier by id")
    public ResponseEntity<ResponseSuccessDTO> findById(
            @Parameter(description = "Supplier auto-generated id") @PathVariable Integer id)
    {
        return ResponseBuilder.success(HttpStatus.OK, supplierQueryService.findById(id));
    }

    @GetMapping
    @Operation(summary = "List all suppliers")
    public ResponseEntity<ResponseSuccessDTO> findAll()
    {
        return ResponseBuilder.success(HttpStatus.OK, supplierQueryService.findAll());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a supplier by id")
    public ResponseEntity<ResponseSuccessDTO> update(
            @Parameter(description = "Supplier auto-generated id") @PathVariable Integer id,
            @RequestBody UpdateSupplierRequestDTO request)
    {
        return ResponseBuilder.success(HttpStatus.OK, supplierUpdateService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a supplier by id")
    public ResponseEntity<ResponseSuccessDTO> delete(
            @Parameter(description = "Supplier auto-generated id") @PathVariable Integer id)
    {
        supplierDeleteService.deleteById(id);
        return ResponseBuilder.success(HttpStatus.OK, null);
    }
}
