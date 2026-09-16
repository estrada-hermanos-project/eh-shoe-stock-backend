package com.estradahermanos.shoestock.controller;

import com.estradahermanos.shoestock.dto.request.RegisterStockRequestDTO;
import com.estradahermanos.shoestock.dto.response.ResponseSuccessDTO;
import com.estradahermanos.shoestock.service.InventoryDecreaseService;
import com.estradahermanos.shoestock.service.InventoryQueryService;
import com.estradahermanos.shoestock.service.InventoryRegisterService;
import com.estradahermanos.shoestock.utilities.ResponseBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Shoe stock (inventory)")
public class InventoryController
{
    private final InventoryRegisterService inventoryRegisterService;
    private final InventoryDecreaseService inventoryDecreaseService;
    private final InventoryQueryService    inventoryQueryService;

    @PostMapping
    @Operation(summary = "Register incoming merchandise (create or increase a variant stock)")
    public ResponseEntity<ResponseSuccessDTO> register(@RequestBody RegisterStockRequestDTO request)
    {
        return ResponseBuilder.success(HttpStatus.OK, inventoryRegisterService.register(request));
    }

    @PostMapping("/decrease")
    @Operation(summary = "Decrease the stock of an existing variant (consumed by Sales)")
    public ResponseEntity<ResponseSuccessDTO> decrease(@RequestBody RegisterStockRequestDTO request)
    {
        return ResponseBuilder.success(HttpStatus.OK, inventoryDecreaseService.decrease(request));
    }

    @GetMapping
    @Operation(summary = "Query the stock of a variant by shoe, color and size")
    public ResponseEntity<ResponseSuccessDTO> findStock(
            @Parameter(description = "Shoe code") @RequestParam("shoe_id") String shoeId,
            @Parameter(description = "Variant color") @RequestParam String color,
            @Parameter(description = "Variant size") @RequestParam Integer size)
    {
        return ResponseBuilder.success(HttpStatus.OK, inventoryQueryService.findStock(shoeId, color, size));
    }
}
