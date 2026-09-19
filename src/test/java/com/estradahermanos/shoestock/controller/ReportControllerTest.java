package com.estradahermanos.shoestock.controller;

import com.estradahermanos.shoestock.config.AuthTokenInterceptor;
import com.estradahermanos.shoestock.config.SecurityConfig;
import com.estradahermanos.shoestock.config.WebMvcConfig;
import com.estradahermanos.shoestock.dto.request.SalesReportFilterDTO;
import com.estradahermanos.shoestock.dto.response.ProductSalesRankDTO;
import com.estradahermanos.shoestock.error.ControllerExceptionHandler;
import com.estradahermanos.shoestock.service.ReportInventoryQueryService;
import com.estradahermanos.shoestock.service.ReportMerchandiseQueryService;
import com.estradahermanos.shoestock.service.ReportSalesQueryService;
import com.estradahermanos.shoestock.service.ReportSummaryQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReportController.class)
@Import({SecurityConfig.class, WebMvcConfig.class, AuthTokenInterceptor.class, ControllerExceptionHandler.class})
@TestPropertySource(properties = "app.auth-token=test-token")
class ReportControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportSalesQueryService reportSalesQueryService;

    @MockitoBean
    private ReportInventoryQueryService reportInventoryQueryService;

    @MockitoBean
    private ReportMerchandiseQueryService reportMerchandiseQueryService;

    @MockitoBean
    private ReportSummaryQueryService reportSummaryQueryService;

    @Test
    void rejectsMissingAuthToken() throws Exception
    {
        mockMvc.perform(get("/api/v1/reports/sales/top-products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void topProductsReturnsOk() throws Exception
    {
        when(reportSalesQueryService.topProducts(any(SalesReportFilterDTO.class)))
                .thenReturn(List.of(ProductSalesRankDTO.builder()
                        .shoeStockId(15)
                        .shoeName("Oxford clasico")
                        .amountSold(18L)
                        .build()));

        mockMvc.perform(get("/api/v1/reports/sales/top-products")
                        .header("auth-token", "test-token")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data[0].shoe_stock_id").value(15))
                .andExpect(jsonPath("$.data[0].amount_sold").value(18));
    }

    @Test
    void inventoryStatusReturnsOk() throws Exception
    {
        when(reportInventoryQueryService.status(any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/reports/inventory")
                        .header("auth-token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }
}
