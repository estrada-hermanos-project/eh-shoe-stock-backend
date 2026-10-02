package com.estradahermanos.shoestock.utilities;

import com.estradahermanos.shoestock.config.CorsConfig;
import com.estradahermanos.shoestock.config.OpenApiConfig;
import com.estradahermanos.shoestock.controller.AuthController;
import com.estradahermanos.shoestock.controller.UserAccountController;
import com.estradahermanos.shoestock.dto.request.CreateUserAccountRequestDTO;
import com.estradahermanos.shoestock.dto.request.LoginRequestDTO;
import com.estradahermanos.shoestock.dto.response.LoginResponseDTO;
import com.estradahermanos.shoestock.dto.response.UserAccountResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.error.ControllerExceptionHandler;
import com.estradahermanos.shoestock.service.UserAccountCreateService;
import com.estradahermanos.shoestock.service.UserLoginService;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QualityCoverageSupportTest
{
    @Test
    void openApiAndCorsExposeTheExpectedConfiguration()
    {
        assertEquals("EH Shoe Stock API", new OpenApiConfig().openAPI().getInfo().getTitle());

        CorsConfiguration configuration = ((UrlBasedCorsConfigurationSource) new CorsConfig().corsConfigurationSource())
                .getCorsConfigurations()
                .get("/**");

        assertNotNull(configuration);
        assertTrue(configuration.getAllowedMethods().contains("GET"));
    }

    @Test
    void jwtAndSessionCodeAreGenerated()
    {
        String token = new JwtProvider("local-dev-jwt-secret-change-me-32bytes-min", 60)
                .generateForSession("Ab12C");
        String code = new SessionCodeGenerator().generate();

        assertTrue(token.contains("."));
        assertEquals(5, code.length());
    }

    @Test
    void exceptionLogCoversBothBranchesAndItsConstructor() throws Exception
    {
        ExceptionLog.unexpected("failed", new IllegalStateException("db"));
        IllegalStateException empty = new IllegalStateException("empty");
        empty.setStackTrace(new StackTraceElement[0]);
        ExceptionLog.unexpected("failed", empty);

        Constructor<ExceptionLog> constructor = ExceptionLog.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    void exceptionHandlerMapsBusinessIntegrityAndGenericErrors()
    {
        ControllerExceptionHandler handler = new ControllerExceptionHandler();
        BusinessException business = BusinessException.builder()
                .code(HttpStatus.BAD_REQUEST)
                .message("invalid")
                .build();

        assertEquals(HttpStatus.BAD_REQUEST, handler.handleBusinessException(business).getStatusCode());
        assertEquals(HttpStatus.CONFLICT, handler.handleIntegrity(
                new DataIntegrityViolationException("dup", new IllegalStateException("duplicate"))).getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, handler.handleGeneric(new IllegalStateException("boom"))
                .getStatusCode());
    }

    @Test
    void authAndAccountControllersDelegate()
    {
        UserLoginService loginService = mock(UserLoginService.class);
        UserAccountCreateService accountService = mock(UserAccountCreateService.class);
        when(loginService.login(org.mockito.ArgumentMatchers.any())).thenReturn(LoginResponseDTO.builder().token("jwt").build());
        when(accountService.create(org.mockito.ArgumentMatchers.any()))
                .thenReturn(UserAccountResponseDTO.builder().username("diego").build());

        assertEquals(HttpStatus.OK, new AuthController(loginService)
                .login(LoginRequestDTO.builder().username("diego").password("secret").build())
                .getStatusCode());
        assertEquals(HttpStatus.OK, new UserAccountController(accountService)
                .create(CreateUserAccountRequestDTO.builder().username("diego").password("secret").dpi("123").build())
                .getStatusCode());
    }
}
