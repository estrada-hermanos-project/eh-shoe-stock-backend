package com.estradahermanos.shoestock.utilities;

import com.estradahermanos.shoestock.error.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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
