package com.estradahermanos.shoestock.controller;

import com.estradahermanos.shoestock.dto.HealthResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "Service availability")
public class HealthController
{
    @GetMapping
    @Operation(summary = "Check service status")
    public ResponseEntity<HealthResponseDTO> health()
    {
        return ResponseEntity.ok(new HealthResponseDTO("UP"));
    }
}
