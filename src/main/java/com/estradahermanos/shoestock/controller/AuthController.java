package com.estradahermanos.shoestock.controller;

import com.estradahermanos.shoestock.dto.request.LoginRequestDTO;
import com.estradahermanos.shoestock.dto.response.ResponseSuccessDTO;
import com.estradahermanos.shoestock.service.UserLoginService;
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
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Administrative user authentication")
public class AuthController
{
    private final UserLoginService userLoginService;

    @PostMapping("/login")
    @Operation(summary = "Authenticate an administrator and open a session, returning a JWT")
    public ResponseEntity<ResponseSuccessDTO> login(@RequestBody LoginRequestDTO request)
    {
        return ResponseBuilder.success(HttpStatus.OK, userLoginService.login(request));
    }
}
