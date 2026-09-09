package com.estradahermanos.shoestock.controller;

import com.estradahermanos.shoestock.config.AuthTokenInterceptor;
import com.estradahermanos.shoestock.config.SecurityConfig;
import com.estradahermanos.shoestock.config.WebMvcConfig;
import com.estradahermanos.shoestock.dto.request.CreateUserAdminRequestDTO;
import com.estradahermanos.shoestock.dto.request.UpdateUserAdminRequestDTO;
import com.estradahermanos.shoestock.dto.response.UserAdminResponseDTO;
import com.estradahermanos.shoestock.error.ControllerExceptionHandler;
import com.estradahermanos.shoestock.service.UserAdminCreateService;
import com.estradahermanos.shoestock.service.UserAdminDeleteService;
import com.estradahermanos.shoestock.service.UserAdminQueryService;
import com.estradahermanos.shoestock.service.UserAdminUpdateService;
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

@WebMvcTest(controllers = UserAdminController.class)
@Import({SecurityConfig.class, WebMvcConfig.class, AuthTokenInterceptor.class, ControllerExceptionHandler.class})
@TestPropertySource(properties = "app.auth-token=test-token")
class UserAdminControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserAdminCreateService userAdminCreateService;

    @MockitoBean
    private UserAdminQueryService userAdminQueryService;

    @MockitoBean
    private UserAdminUpdateService userAdminUpdateService;

    @MockitoBean
    private UserAdminDeleteService userAdminDeleteService;

    @Test
    void rejectsMissingAuthToken() throws Exception
    {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createReturnsCreated() throws Exception
    {
        CreateUserAdminRequestDTO request = CreateUserAdminRequestDTO.builder()
                .dpi("1234567890123")
                .fullName("Ana Lopez")
                .phone("55551234")
                .email("ana@gmail.com")
                .build();
        when(userAdminCreateService.create(any())).thenReturn(
                UserAdminResponseDTO.builder().name("Ana Lopez").phone("55551234").email("ana@gmail.com").build());

        mockMvc.perform(post("/api/v1/users")
                        .header("auth-token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Ana Lopez"));
    }

    @Test
    void findAllReturnsList() throws Exception
    {
        when(userAdminQueryService.findAll()).thenReturn(
                List.of(UserAdminResponseDTO.builder().name("Ana Lopez").phone("55551234").build()));

        mockMvc.perform(get("/api/v1/users").header("auth-token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Ana Lopez"));
    }

    @Test
    void findByDpiReturnsUser() throws Exception
    {
        when(userAdminQueryService.findByDpi("1234567890123")).thenReturn(
                UserAdminResponseDTO.builder().name("Ana Lopez").phone("55551234").build());

        mockMvc.perform(get("/api/v1/users/1234567890123").header("auth-token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Ana Lopez"));
    }

    @Test
    void updateReturnsOk() throws Exception
    {
        UpdateUserAdminRequestDTO request = UpdateUserAdminRequestDTO.builder()
                .fullName("Ana Lopez")
                .phone("55551234")
                .build();
        when(userAdminUpdateService.update(eq("1234567890123"), any())).thenReturn(
                UserAdminResponseDTO.builder().name("Ana Lopez").phone("55551234").build());

        mockMvc.perform(put("/api/v1/users/1234567890123")
                        .header("auth-token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.phone").value("55551234"));
    }

    @Test
    void deleteReturnsOk() throws Exception
    {
        doNothing().when(userAdminDeleteService).deleteByDpi("1234567890123");

        mockMvc.perform(delete("/api/v1/users/1234567890123").header("auth-token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());
    }
}
