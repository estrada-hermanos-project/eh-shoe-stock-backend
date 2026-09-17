package com.estradahermanos.shoestock.controller;

import com.estradahermanos.shoestock.config.AuthTokenInterceptor;
import com.estradahermanos.shoestock.config.SecurityConfig;
import com.estradahermanos.shoestock.config.WebMvcConfig;
import com.estradahermanos.shoestock.dto.request.CreateSupplierRequestDTO;
import com.estradahermanos.shoestock.dto.request.UpdateSupplierRequestDTO;
import com.estradahermanos.shoestock.dto.response.SupplierResponseDTO;
import com.estradahermanos.shoestock.error.ControllerExceptionHandler;
import com.estradahermanos.shoestock.service.SupplierCreateService;
import com.estradahermanos.shoestock.service.SupplierDeleteService;
import com.estradahermanos.shoestock.service.SupplierQueryService;
import com.estradahermanos.shoestock.service.SupplierUpdateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SupplierController.class)
@Import({SecurityConfig.class, WebMvcConfig.class, AuthTokenInterceptor.class, ControllerExceptionHandler.class})
@TestPropertySource(properties = "app.auth-token=test-token")
class SupplierControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SupplierCreateService supplierCreateService;

    @MockitoBean
    private SupplierQueryService supplierQueryService;

    @MockitoBean
    private SupplierUpdateService supplierUpdateService;

    @MockitoBean
    private SupplierDeleteService supplierDeleteService;

    @Test
    void rejectsMissingAuthToken() throws Exception
    {
        mockMvc.perform(get("/api/v1/suppliers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createReturnsCreated() throws Exception
    {
        CreateSupplierRequestDTO request = CreateSupplierRequestDTO.builder()
                .fullName("Carlos Estrada")
                .phone("55512345")
                .build();
        when(supplierCreateService.create(any())).thenReturn(
                SupplierResponseDTO.builder().id(5).name("Carlos Estrada").phone("55512345").build());

        mockMvc.perform(post("/api/v1/suppliers")
                        .header("auth-token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Carlos Estrada"));
    }

    @Test
    void findAllReturnsList() throws Exception
    {
        when(supplierQueryService.findAll()).thenReturn(
                List.of(SupplierResponseDTO.builder().id(5).name("Carlos Estrada").phone("55512345").build()));

        mockMvc.perform(get("/api/v1/suppliers").header("auth-token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Carlos Estrada"));
    }

    @Test
    void findByIdReturnsSupplier() throws Exception
    {
        when(supplierQueryService.findById(5)).thenReturn(
                SupplierResponseDTO.builder().id(5).name("Carlos Estrada").phone("55512345").build());

        mockMvc.perform(get("/api/v1/suppliers/5").header("auth-token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Carlos Estrada"));
    }

    @Test
    void updateReturnsOk() throws Exception
    {
        UpdateSupplierRequestDTO request = UpdateSupplierRequestDTO.builder()
                .fullName("Carlos Estrada")
                .phone("55512345")
                .build();
        when(supplierUpdateService.update(eq(5), any())).thenReturn(
                SupplierResponseDTO.builder().id(5).name("Carlos Estrada").phone("55512345").build());

        mockMvc.perform(put("/api/v1/suppliers/5")
                        .header("auth-token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.phone").value("55512345"));
    }

    @Test
    void deleteReturnsOk() throws Exception
    {
        doNothing().when(supplierDeleteService).deleteById(5);

        mockMvc.perform(delete("/api/v1/suppliers/5").header("auth-token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());
    }
}
