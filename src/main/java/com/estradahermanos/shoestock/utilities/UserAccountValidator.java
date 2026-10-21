package com.estradahermanos.shoestock.utilities;

import com.estradahermanos.shoestock.error.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Component
public class UserAccountValidator
{
    public void validateCredentials(String username, String password)
    {
        requireField(username, "Username is required");
        requireField(password, "Password is required");
    }

    public void validateAccountCreation(String username, String password, String dpi)
    {
        validateCredentials(username, password);
        requireField(dpi, "DPI is required");
    }

    public void validateUsername(String username)
    {
        requireField(username, "Username is required");
    }

    public void validatePasswordReset(String username, String newPassword, String confirmation, String token)
    {
        validateUsername(username);
        requireField(newPassword, "Password is required");
        requireField(confirmation, "Password confirmation is required");
        requireField(token, "Recovery token is required");
        if (!Objects.equals(newPassword, confirmation))
        {
            throw BusinessException.builder()
                    .code(HttpStatus.BAD_REQUEST)
                    .message("Password confirmation does not match")
                    .build();
        }
    }

    private void requireField(String value, String message)
    {
        if (!StringUtils.hasText(value))
        {
            throw BusinessException.builder()
                    .code(HttpStatus.BAD_REQUEST)
                    .message(message)
                    .build();
        }
    }
}
