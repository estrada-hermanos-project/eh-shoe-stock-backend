package com.estradahermanos.shoestock.controller;

import com.estradahermanos.shoestock.config.AuthTokenInterceptor;
import com.estradahermanos.shoestock.config.SecurityConfig;
import com.estradahermanos.shoestock.config.WebMvcConfig;
import com.estradahermanos.shoestock.dto.response.MessageResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.error.ControllerExceptionHandler;
import com.estradahermanos.shoestock.service.PasswordRecoveryRequestService;
import com.estradahermanos.shoestock.service.PasswordResetService;
import com.estradahermanos.shoestock.service.UserLoginService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@Import({SecurityConfig.class, WebMvcConfig.class, AuthTokenInterceptor.class, ControllerExceptionHandler.class})
@TestPropertySource(properties = "app.auth-token=test-token")
class AuthControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserLoginService userLoginService;

    @MockitoBean
    private PasswordRecoveryRequestService passwordRecoveryRequestService;

    @MockitoBean
    private PasswordResetService passwordResetService;

    @Test
    void recoveryRequiresAuthToken() throws Exception
    {
        mockMvc.perform(post("/api/v1/auth/password-recovery")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"diego\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void resetRequiresAuthToken() throws Exception
    {
        mockMvc.perform(post("/api/v1/auth/password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"diego\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void recoveryReturnsThePublicMessage() throws Exception
    {
        when(passwordRecoveryRequestService.request(any())).thenReturn(
                MessageResponseDTO.builder().message("If the account exists, a recovery email was sent").build());

        mockMvc.perform(post("/api/v1/auth/password-recovery")
                        .header("auth-token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"diego\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("If the account exists, a recovery email was sent"));
    }

    @Test
    void resetReadsSnakeCaseAndReturnsOk() throws Exception
    {
        when(passwordResetService.reset(any())).thenReturn(
                MessageResponseDTO.builder().message("Password updated").build());

        mockMvc.perform(post("/api/v1/auth/password-reset")
                        .header("auth-token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"diego","new_password":"nueva","confirm_password":"nueva","token":"raw-token"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("Password updated"));
    }

    @Test
    void recoveryFailureStaysControlled() throws Exception
    {
        when(passwordRecoveryRequestService.request(any())).thenThrow(BusinessException.builder()
                .code(HttpStatus.BAD_GATEWAY)
                .message("Could not send email")
                .build());

        mockMvc.perform(post("/api/v1/auth/password-recovery")
                        .header("auth-token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"diego\"}"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.message").value("Could not send email"));
    }
}
