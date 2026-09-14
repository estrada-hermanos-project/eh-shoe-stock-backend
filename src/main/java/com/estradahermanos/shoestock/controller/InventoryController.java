package com.estradahermanos.shoestock.controller;

import com.estradahermanos.shoestock.dto.request.RegisterStockRequestDTO;
import com.estradahermanos.shoestock.dto.response.ResponseSuccessDTO;
import com.estradahermanos.shoestock.service.InventoryRegisterService;
import com.estradahermanos.shoestock.utilities.ResponseBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Shoe stock (inventory)")
public class InventoryController
{
    private final InventoryRegisterService inventoryRegisterService;

    @PostMapping
    @Operation(summary = "Register incoming merchandise (create or increase a variant stock)")
    public ResponseEntity<ResponseSuccessDTO> register(@RequestBody RegisterStockRequestDTO request)
    {
        return ResponseBuilder.success(HttpStatus.OK, inventoryRegisterService.register(request));
    }
}
