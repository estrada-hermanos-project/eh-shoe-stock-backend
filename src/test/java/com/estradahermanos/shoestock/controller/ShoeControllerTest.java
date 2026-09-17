package com.estradahermanos.shoestock.controller;

import com.estradahermanos.shoestock.config.AuthTokenInterceptor;
import com.estradahermanos.shoestock.config.SecurityConfig;
import com.estradahermanos.shoestock.config.WebMvcConfig;
import com.estradahermanos.shoestock.dto.request.CreateShoeRequestDTO;
import com.estradahermanos.shoestock.dto.response.ShoeResponseDTO;
import com.estradahermanos.shoestock.error.ControllerExceptionHandler;
import com.estradahermanos.shoestock.service.ShoeCreateService;
import com.estradahermanos.shoestock.service.ShoeDeleteService;
import com.estradahermanos.shoestock.service.ShoeQueryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ShoeController.class)
@Import({SecurityConfig.class, WebMvcConfig.class, AuthTokenInterceptor.class, ControllerExceptionHandler.class})
@TestPropertySource(properties = "app.auth-token=test-token")
class ShoeControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ShoeCreateService shoeCreateService;

    @MockitoBean
    private ShoeQueryService shoeQueryService;

    @MockitoBean
    private ShoeDeleteService shoeDeleteService;

    @Test
    void rejectsMissingAuthToken() throws Exception
    {
        mockMvc.perform(get("/api/v1/shoes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createReturnsCreated() throws Exception
    {
        CreateShoeRequestDTO request = CreateShoeRequestDTO.builder()
                .code("OXF-001")
                .type("Caballero")
                .name("Oxford clasico")
                .supplierId(2)
                .build();
        when(shoeCreateService.create(any())).thenReturn(
                ShoeResponseDTO.builder().code("OXF-001").supplierName("Maria Lopez").build());

        mockMvc.perform(post("/api/v1/shoes")
                        .header("auth-token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.code").value("OXF-001"))
                .andExpect(jsonPath("$.data.supplier_name").value("Maria Lopez"));
    }

    @Test
    void findByCodeReturnsShoe() throws Exception
    {
        when(shoeQueryService.findByCode("OXF-001")).thenReturn(
                ShoeResponseDTO.builder().code("OXF-001").name("Oxford").build());

        mockMvc.perform(get("/api/v1/shoes/OXF-001").header("auth-token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("OXF-001"));
    }

    @Test
    void findAllReturnsList() throws Exception
    {
        when(shoeQueryService.findAll()).thenReturn(
                List.of(ShoeResponseDTO.builder().code("OXF-001").build()));

        mockMvc.perform(get("/api/v1/shoes").header("auth-token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].code").value("OXF-001"));
    }

    @Test
    void findBySupplierReturnsList() throws Exception
    {
        when(shoeQueryService.findBySupplier(2)).thenReturn(
                List.of(ShoeResponseDTO.builder().code("OXF-001").supplierName("Maria Lopez").build()));

        mockMvc.perform(get("/api/v1/shoes/supplier/2").header("auth-token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].supplier_name").value("Maria Lopez"));
    }

    @Test
    void deleteReturnsOk() throws Exception
    {
        doNothing().when(shoeDeleteService).deleteByCode("OXF-001");

        mockMvc.perform(delete("/api/v1/shoes/OXF-001").header("auth-token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());
    }
}
