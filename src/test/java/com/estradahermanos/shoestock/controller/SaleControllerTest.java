package com.estradahermanos.shoestock.controller;

import com.estradahermanos.shoestock.config.AuthTokenInterceptor;
import com.estradahermanos.shoestock.config.SecurityConfig;
import com.estradahermanos.shoestock.config.WebMvcConfig;
import com.estradahermanos.shoestock.dto.request.CreateSaleRequestDTO;
import com.estradahermanos.shoestock.dto.response.SaleResponseDTO;
import com.estradahermanos.shoestock.error.ControllerExceptionHandler;
import com.estradahermanos.shoestock.service.SaleQueryService;
import com.estradahermanos.shoestock.service.SaleRegisterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SaleController.class)
@Import({SecurityConfig.class, WebMvcConfig.class, AuthTokenInterceptor.class, ControllerExceptionHandler.class})
@TestPropertySource(properties = "app.auth-token=test-token")
class SaleControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SaleRegisterService saleRegisterService;

    @MockitoBean
    private SaleQueryService saleQueryService;

    private SaleResponseDTO saleResponse()
    {
        return SaleResponseDTO.builder()
                .size(40)
                .name("Oxford clasico")
                .color("Negro")
                .stock(2)
                .saleDate(LocalDate.of(2026, 9, 14))
                .build();
    }

    @Test
    void rejectsMissingAuthToken() throws Exception
    {
        mockMvc.perform(post("/api/v1/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerReturnsCreated() throws Exception
    {
        CreateSaleRequestDTO request = CreateSaleRequestDTO.builder()
                .size(40)
                .name("Oxford clasico")
                .color("Negro")
                .stock(2)
                .build();
        when(saleRegisterService.register(any())).thenReturn(saleResponse());

        mockMvc.perform(post("/api/v1/sales")
                        .header("auth-token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.size").value(40))
                .andExpect(jsonPath("$.data.name").value("Oxford clasico"))
                .andExpect(jsonPath("$.data.color").value("Negro"))
                .andExpect(jsonPath("$.data.stock").value(2))
                .andExpect(jsonPath("$.data.sale_date").value("2026-09-14"));
    }

    @Test
    void findByRangeReturnsList() throws Exception
    {
        when(saleQueryService.findByDateRange(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 14)))
                .thenReturn(List.of(saleResponse()));

        mockMvc.perform(get("/api/v1/sales")
                        .header("auth-token", "test-token")
                        .param("start_date", "2026-09-01")
                        .param("end_date", "2026-09-14"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Oxford clasico"))
                .andExpect(jsonPath("$.data[0].sale_date").value("2026-09-14"));
    }

    @Test
    void findByDateReturnsList() throws Exception
    {
        when(saleQueryService.findByDate(LocalDate.of(2026, 9, 14)))
                .thenReturn(List.of(saleResponse()));

        mockMvc.perform(get("/api/v1/sales/by-date")
                        .header("auth-token", "test-token")
                        .param("date", "2026-09-14"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].stock").value(2))
                .andExpect(jsonPath("$.data[0].size").value(40));
    }
}
