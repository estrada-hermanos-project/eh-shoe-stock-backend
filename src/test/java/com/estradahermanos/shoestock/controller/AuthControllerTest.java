package com.estradahermanos.shoestock.controller;

import com.estradahermanos.shoestock.config.AuthTokenInterceptor;
import com.estradahermanos.shoestock.config.SecurityConfig;
import com.estradahermanos.shoestock.config.WebMvcConfig;
import com.estradahermanos.shoestock.dto.response.LoginChallengeResponseDTO;
import com.estradahermanos.shoestock.dto.response.LoginResponseDTO;
import com.estradahermanos.shoestock.dto.response.MessageResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.error.ControllerExceptionHandler;
import com.estradahermanos.shoestock.service.LoginCodeVerifyService;
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

    @MockitoBean
    private LoginCodeVerifyService loginCodeVerifyService;

    @Test
    void loginRequiresAuthToken() throws Exception
    {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"diego\",\"password\":\"secret\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void verifyRequiresAuthToken() throws Exception
    {
        mockMvc.perform(post("/api/v1/auth/login/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"diego\",\"code\":\"1234\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginAsksForTheSecondStepAndDoesNotReturnAToken() throws Exception
    {
        when(userLoginService.login(any())).thenReturn(LoginChallengeResponseDTO.builder()
                .secondStepRequired(true)
                .message("A verification code was sent to the administrator email")
                .build());

        mockMvc.perform(post("/api/v1/auth/login")
                        .header("auth-token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"diego\",\"password\":\"secret\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.second_step_required").value(true))
                .andExpect(jsonPath("$.data.message")
                        .value("A verification code was sent to the administrator email"))
                .andExpect(jsonPath("$.data.token").doesNotExist());
    }

    @Test
    void verifyReturnsTheSessionToken() throws Exception
    {
        when(loginCodeVerifyService.verify(any())).thenReturn(LoginResponseDTO.builder().token("jwt-token").build());

        mockMvc.perform(post("/api/v1/auth/login/verify")
                        .header("auth-token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"diego\",\"code\":\"1234\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("jwt-token"));
    }

    @Test
    void verifyExposesTheIncorrectCodeMessage() throws Exception
    {
        when(loginCodeVerifyService.verify(any())).thenThrow(BusinessException.builder()
                .code(HttpStatus.UNAUTHORIZED)
                .message("El codigo es incorrecto")
                .build());

        mockMvc.perform(post("/api/v1/auth/login/verify")
                        .header("auth-token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"diego\",\"code\":\"1234\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("El codigo es incorrecto"));
    }

    @Test
    void verifyExposesTheExpiredCodeMessage() throws Exception
    {
        when(loginCodeVerifyService.verify(any())).thenThrow(BusinessException.builder()
                .code(HttpStatus.BAD_REQUEST)
                .message("El código ha expirado")
                .build());

        mockMvc.perform(post("/api/v1/auth/login/verify")
                        .header("auth-token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"diego\",\"code\":\"1234\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El código ha expirado"));
    }

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
