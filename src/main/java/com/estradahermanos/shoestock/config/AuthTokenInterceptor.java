package com.estradahermanos.shoestock.config;

import com.estradahermanos.shoestock.error.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthTokenInterceptor implements HandlerInterceptor
{
    public static final String AUTH_TOKEN_HEADER = "auth-token";

    private final String validAuthToken;

    public AuthTokenInterceptor(@Value("${app.auth-token}") String validAuthToken)
    {
        this.validAuthToken = validAuthToken;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
    {
        String requestToken = request.getHeader(AUTH_TOKEN_HEADER);
        if (!StringUtils.hasText(validAuthToken) || !validAuthToken.equals(requestToken))
        {
            throw BusinessException.builder()
                    .code(HttpStatus.UNAUTHORIZED)
                    .message("Invalid or missing auth-token header")
                    .build();
        }
        return true;
    }
}
