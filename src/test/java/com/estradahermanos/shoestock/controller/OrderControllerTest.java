package com.estradahermanos.shoestock.controller;

import com.estradahermanos.shoestock.config.AuthTokenInterceptor;
import com.estradahermanos.shoestock.config.SecurityConfig;
import com.estradahermanos.shoestock.config.WebMvcConfig;
import com.estradahermanos.shoestock.dto.request.CreateOrderDetailRequestDTO;
import com.estradahermanos.shoestock.dto.request.CreateOrderRequestDTO;
import com.estradahermanos.shoestock.dto.response.OrderDetailResponseDTO;
import com.estradahermanos.shoestock.dto.response.OrderResponseDTO;
import com.estradahermanos.shoestock.error.ControllerExceptionHandler;
import com.estradahermanos.shoestock.service.OrderCreateService;
import com.estradahermanos.shoestock.service.OrderDetailCreateService;
import com.estradahermanos.shoestock.service.OrderQueryService;
import com.estradahermanos.shoestock.service.OrderReceiveService;
import com.estradahermanos.shoestock.utilities.OrderStatusEnum;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderController.class)
@Import({SecurityConfig.class, WebMvcConfig.class, AuthTokenInterceptor.class, ControllerExceptionHandler.class})
@TestPropertySource(properties = "app.auth-token=test-token")
class OrderControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderCreateService orderCreateService;

    @MockitoBean
    private OrderDetailCreateService orderDetailCreateService;

    @MockitoBean
    private OrderQueryService orderQueryService;

    @MockitoBean
    private OrderReceiveService orderReceiveService;

    private OrderResponseDTO orderResponse()
    {
        return OrderResponseDTO.builder()
                .orderId("ORDEN-101")
                .supplierName("Maria Lopez")
                .status(OrderStatusEnum.PENDIENTE)
                .creationDate(LocalDate.of(2026, 9, 16))
                .details(List.of(OrderDetailResponseDTO.builder()
                        .shoeStockId(15)
                        .shoeName("Oxford clasico")
                        .amount(4)
                        .build()))
                .build();
    }

    @Test
    void rejectsMissingAuthToken() throws Exception
    {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createReturnsCreated() throws Exception
    {
        CreateOrderRequestDTO request = CreateOrderRequestDTO.builder()
                .id("ORDEN-101")
                .supplierId(2)
                .build();
        when(orderCreateService.create(any())).thenReturn(orderResponse());

        mockMvc.perform(post("/api/v1/orders")
                        .header("auth-token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.order_id").value("ORDEN-101"))
                .andExpect(jsonPath("$.data.supplier_name").value("Maria Lopez"))
                .andExpect(jsonPath("$.data.status").value("PENDIENTE"))
                .andExpect(jsonPath("$.data.creation_date").value("2026-09-16"));
    }

    @Test
    void addDetailReturnsCreated() throws Exception
    {
        CreateOrderDetailRequestDTO request = CreateOrderDetailRequestDTO.builder()
                .shoeStockId(15)
                .amount(4)
                .build();
        when(orderDetailCreateService.create(eq("ORDEN-101"), any())).thenReturn(orderResponse());

        mockMvc.perform(post("/api/v1/orders/ORDEN-101/details")
                        .header("auth-token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.details[0].shoe_stock_id").value(15))
                .andExpect(jsonPath("$.data.details[0].shoe_name").value("Oxford clasico"))
                .andExpect(jsonPath("$.data.details[0].amount").value(4));
    }

    @Test
    void findByIdReturnsOrder() throws Exception
    {
        when(orderQueryService.findById("ORDEN-101")).thenReturn(orderResponse());

        mockMvc.perform(get("/api/v1/orders/ORDEN-101")
                        .header("auth-token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.order_id").value("ORDEN-101"));
    }

    @Test
    void findByRangeReturnsList() throws Exception
    {
        when(orderQueryService.findByDateRange(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 16)))
                .thenReturn(List.of(orderResponse()));

        mockMvc.perform(get("/api/v1/orders")
                        .header("auth-token", "test-token")
                        .param("start_date", "2026-09-01")
                        .param("end_date", "2026-09-16"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].order_id").value("ORDEN-101"));
    }

    @Test
    void findByStatusReturnsList() throws Exception
    {
        when(orderQueryService.findByStatus("PENDIENTE")).thenReturn(List.of(orderResponse()));

        mockMvc.perform(get("/api/v1/orders/by-status")
                        .header("auth-token", "test-token")
                        .param("status", "PENDIENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").value("PENDIENTE"));
    }

    @Test
    void findBySupplierReturnsList() throws Exception
    {
        when(orderQueryService.findBySupplier(2)).thenReturn(List.of(orderResponse()));

        mockMvc.perform(get("/api/v1/orders/supplier/2")
                        .header("auth-token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].supplier_name").value("Maria Lopez"));
    }

    @Test
    void receiveReturnsOk() throws Exception
    {
        OrderResponseDTO received = orderResponse();
        received.setStatus(OrderStatusEnum.RECIBIDA);
        when(orderReceiveService.receive("ORDEN-101")).thenReturn(received);

        mockMvc.perform(post("/api/v1/orders/ORDEN-101/receive")
                        .header("auth-token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RECIBIDA"));
    }
}
