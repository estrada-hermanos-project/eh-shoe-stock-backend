package com.estradahermanos.shoestock.utilities;

import com.estradahermanos.shoestock.error.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class SaleValidator
{
    public void validateCreate(String name, String color)
    {
        requireField(name, "Name is required");
        requireField(color, "Color is required");
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
