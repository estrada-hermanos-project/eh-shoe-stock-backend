package com.estradahermanos.shoestock.controller;

import com.estradahermanos.shoestock.dto.request.CreateUserAdminRequestDTO;
import com.estradahermanos.shoestock.dto.request.UpdateUserAdminRequestDTO;
import com.estradahermanos.shoestock.dto.response.ResponseSuccessDTO;
import com.estradahermanos.shoestock.service.UserAdminCreateService;
import com.estradahermanos.shoestock.service.UserAdminDeleteService;
import com.estradahermanos.shoestock.service.UserAdminQueryService;
import com.estradahermanos.shoestock.service.UserAdminUpdateService;
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
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Admin", description = "Administrative user master data")
public class UserAdminController
{
    private final UserAdminCreateService userAdminCreateService;
    private final UserAdminQueryService  userAdminQueryService;
    private final UserAdminUpdateService userAdminUpdateService;
    private final UserAdminDeleteService userAdminDeleteService;

    @PostMapping
    @Operation(summary = "Register a new administrative user")
    public ResponseEntity<ResponseSuccessDTO> create(@RequestBody CreateUserAdminRequestDTO request)
    {
        return ResponseBuilder.success(HttpStatus.CREATED, userAdminCreateService.create(request));
    }

    @GetMapping("/{dpi}")
    @Operation(summary = "Get an administrative user by DPI")
    public ResponseEntity<ResponseSuccessDTO> findByDpi(
            @Parameter(description = "Guatemalan DPI, 13 digits") @PathVariable String dpi)
    {
        return ResponseBuilder.success(HttpStatus.OK, userAdminQueryService.findByDpi(dpi));
    }

    @GetMapping
    @Operation(summary = "List all administrative users")
    public ResponseEntity<ResponseSuccessDTO> findAll()
    {
        return ResponseBuilder.success(HttpStatus.OK, userAdminQueryService.findAll());
    }

    @PutMapping("/{dpi}")
    @Operation(summary = "Update an administrative user by DPI")
    public ResponseEntity<ResponseSuccessDTO> update(
            @Parameter(description = "Guatemalan DPI, 13 digits") @PathVariable String dpi,
            @RequestBody UpdateUserAdminRequestDTO request)
    {
        return ResponseBuilder.success(HttpStatus.OK, userAdminUpdateService.update(dpi, request));
    }

    @DeleteMapping("/{dpi}")
    @Operation(summary = "Delete an administrative user by DPI")
    public ResponseEntity<ResponseSuccessDTO> delete(
            @Parameter(description = "Guatemalan DPI, 13 digits") @PathVariable String dpi)
    {
        userAdminDeleteService.deleteByDpi(dpi);
        return ResponseBuilder.success(HttpStatus.OK, null);
    }
}
