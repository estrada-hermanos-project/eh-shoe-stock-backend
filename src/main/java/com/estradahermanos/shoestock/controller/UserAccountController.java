package com.estradahermanos.shoestock.controller;

import com.estradahermanos.shoestock.dto.request.CreateUserAccountRequestDTO;
import com.estradahermanos.shoestock.dto.response.ResponseSuccessDTO;
import com.estradahermanos.shoestock.service.UserAccountCreateService;
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
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "User Account", description = "Administrative user access accounts")
public class UserAccountController
{
    private final UserAccountCreateService userAccountCreateService;

    @PostMapping
    @Operation(summary = "Create an access account for an existing administrative user")
    public ResponseEntity<ResponseSuccessDTO> create(@RequestBody CreateUserAccountRequestDTO request)
    {
        return ResponseBuilder.success(HttpStatus.OK, userAccountCreateService.create(request));
    }
}
