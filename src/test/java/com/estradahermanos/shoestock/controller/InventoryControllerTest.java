package com.estradahermanos.shoestock.controller;

import com.estradahermanos.shoestock.config.AuthTokenInterceptor;
import com.estradahermanos.shoestock.config.SecurityConfig;
import com.estradahermanos.shoestock.config.WebMvcConfig;
import com.estradahermanos.shoestock.dto.request.RegisterStockRequestDTO;
import com.estradahermanos.shoestock.dto.response.ShoeStockResponseDTO;
import com.estradahermanos.shoestock.dto.response.StockQueryResponseDTO;
import com.estradahermanos.shoestock.error.ControllerExceptionHandler;
import com.estradahermanos.shoestock.service.InventoryDecreaseService;
import com.estradahermanos.shoestock.service.InventoryQueryService;
import com.estradahermanos.shoestock.service.InventoryRegisterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InventoryController.class)
@Import({SecurityConfig.class, WebMvcConfig.class, AuthTokenInterceptor.class, ControllerExceptionHandler.class})
@TestPropertySource(properties = "app.auth-token=test-token")
class InventoryControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InventoryRegisterService inventoryRegisterService;

    @MockitoBean
    private InventoryDecreaseService inventoryDecreaseService;

    @MockitoBean
    private InventoryQueryService inventoryQueryService;

    private RegisterStockRequestDTO request()
    {
        return RegisterStockRequestDTO.builder()
                .shoeId("OXF-001")
                .color("Negro")
                .size(40)
                .stock(12)
                .build();
    }

    @Test
    void rejectsMissingAuthToken() throws Exception
    {
        mockMvc.perform(post("/api/v1/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerReturnsOk() throws Exception
    {
        when(inventoryRegisterService.register(any())).thenReturn(
                ShoeStockResponseDTO.builder().id(7).shoeId("OXF-001").color("Negro").size(40).stock(12).build());

        mockMvc.perform(post("/api/v1/inventory")
                        .header("auth-token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.shoe_id").value("OXF-001"))
                .andExpect(jsonPath("$.data.stock").value(12));
    }

    @Test
    void decreaseReturnsOk() throws Exception
    {
        when(inventoryDecreaseService.decrease(any())).thenReturn(
                ShoeStockResponseDTO.builder().id(7).shoeId("OXF-001").color("Negro").size(40).stock(9).build());

        mockMvc.perform(post("/api/v1/inventory/decrease")
                        .header("auth-token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stock").value(9));
    }

    @Test
    void findStockReturnsOk() throws Exception
    {
        when(inventoryQueryService.findStock("OXF-001", "Negro", 40)).thenReturn(
                StockQueryResponseDTO.builder().name("Oxford clasico").color("Negro").size(40).stock(12).build());

        mockMvc.perform(get("/api/v1/inventory")
                        .header("auth-token", "test-token")
                        .param("shoe_id", "OXF-001")
                        .param("color", "Negro")
                        .param("size", "40"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Oxford clasico"))
                .andExpect(jsonPath("$.data.stock").value(12));
    }
}
