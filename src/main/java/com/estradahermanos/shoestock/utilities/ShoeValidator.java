package com.estradahermanos.shoestock.utilities;

import com.estradahermanos.shoestock.error.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ShoeValidator
{
    public void validateCreate(String code, String type, String name)
    {
        requireField(code, "Code is required");
        requireField(name, "Name is required");
        if (!ShoeType.isValid(type))
        {
            throw BusinessException.builder()
                    .code(HttpStatus.BAD_REQUEST)
                    .message("Shoe type must be one of: Dama, Caballero, Niño, Niña")
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
