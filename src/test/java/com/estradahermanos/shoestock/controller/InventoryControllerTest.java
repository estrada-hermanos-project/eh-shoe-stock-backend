package com.estradahermanos.shoestock.controller;

import com.estradahermanos.shoestock.config.AuthTokenInterceptor;
import com.estradahermanos.shoestock.config.SecurityConfig;
import com.estradahermanos.shoestock.config.WebMvcConfig;
import com.estradahermanos.shoestock.dto.request.CreateVariantRequestDTO;
import com.estradahermanos.shoestock.dto.request.RegisterStockRequestDTO;
import com.estradahermanos.shoestock.dto.response.ShoeStockResponseDTO;
import com.estradahermanos.shoestock.dto.response.StockQueryResponseDTO;
import com.estradahermanos.shoestock.error.ControllerExceptionHandler;
import com.estradahermanos.shoestock.dto.response.SupplierVariantResponseDTO;
import com.estradahermanos.shoestock.service.InventoryDecreaseService;
import com.estradahermanos.shoestock.service.InventoryRegisterService;
import com.estradahermanos.shoestock.service.InventoryStyleQueryService;
import com.estradahermanos.shoestock.service.InventoryVariantCreateService;
import com.estradahermanos.shoestock.service.InventorySupplierQueryService;
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
    private InventoryVariantCreateService inventoryVariantCreateService;

    @MockitoBean
    private InventoryDecreaseService inventoryDecreaseService;

    @MockitoBean
    private InventoryStyleQueryService inventoryStyleQueryService;

    @MockitoBean
    private InventorySupplierQueryService inventorySupplierQueryService;

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
    void createVariantReturnsZeroStock() throws Exception
    {
        when(inventoryVariantCreateService.create(any())).thenReturn(
                ShoeStockResponseDTO.builder().id(8).shoeId("OXF-001").color("Cafe").size(38).stock(0).build());

        CreateVariantRequestDTO body = CreateVariantRequestDTO.builder()
                .shoeId("OXF-001").color("Cafe").size(38).build();

        mockMvc.perform(post("/api/v1/inventory/variants")
                        .header("auth-token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.shoe_id").value("OXF-001"))
                .andExpect(jsonPath("$.data.stock").value(0))
                .andExpect(jsonPath("$.data.color").value("Cafe"));
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
        when(inventoryStyleQueryService.findByShoeCode("OXF-001", "Negro", 40)).thenReturn(List.of(
                StockQueryResponseDTO.builder().name("Oxford clasico").color("Negro").size(40).stock(12).build()));

        mockMvc.perform(get("/api/v1/inventory")
                        .header("auth-token", "test-token")
                        .param("shoe_id", "OXF-001")
                        .param("color", "Negro")
                        .param("size", "40"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Oxford clasico"))
                .andExpect(jsonPath("$.data[0].color").value("Negro"))
                .andExpect(jsonPath("$.data[0].stock").value(12));
    }

    @Test
    void findByShoeCodeReturnsVariants() throws Exception
    {
        when(inventoryStyleQueryService.findByShoeCode("OXF-001", null, null)).thenReturn(List.of(
                StockQueryResponseDTO.builder().name("Oxford clasico").color("Negro").size(40).stock(12).build(),
                StockQueryResponseDTO.builder().name("Oxford clasico").color("Cafe").size(41).stock(3).build()));

        mockMvc.perform(get("/api/v1/inventory")
                        .header("auth-token", "test-token")
                        .param("shoe_id", "OXF-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].color").value("Negro"))
                .andExpect(jsonPath("$.data[0].size").value(40))
                .andExpect(jsonPath("$.data[0].stock").value(12))
                .andExpect(jsonPath("$.data[1].stock").value(3));
    }

    @Test
    void findByColorReturnsMatchingVariants() throws Exception
    {
        when(inventoryStyleQueryService.findByShoeCode("OXF-001", "Negro", null)).thenReturn(List.of(
                StockQueryResponseDTO.builder().name("Oxford clasico").color("Negro").size(40).stock(12).build()));

        mockMvc.perform(get("/api/v1/inventory")
                        .header("auth-token", "test-token")
                        .param("shoe_id", "OXF-001")
                        .param("color", "Negro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].color").value("Negro"))
                .andExpect(jsonPath("$.data[0].size").value(40));
    }

    @Test
    void findBySupplierReturnsThatSuppliersVariants() throws Exception
    {
        when(inventorySupplierQueryService.findBySupplier(2)).thenReturn(List.of(
                SupplierVariantResponseDTO.builder()
                        .shoeStockId(15)
                        .shoeCode("OXF-001")
                        .shoeName("Oxford clasico")
                        .color("Negro")
                        .size(40)
                        .build()));

        mockMvc.perform(get("/api/v1/inventory/supplier/2")
                        .header("auth-token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].shoe_stock_id").value(15))
                .andExpect(jsonPath("$.data[0].shoe_code").value("OXF-001"))
                .andExpect(jsonPath("$.data[0].shoe_name").value("Oxford clasico"));
    }
}
