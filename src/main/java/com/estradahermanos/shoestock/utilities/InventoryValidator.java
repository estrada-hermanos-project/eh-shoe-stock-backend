package com.estradahermanos.shoestock.utilities;

import com.estradahermanos.shoestock.error.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class InventoryValidator
{
    public void validateRegister(Integer stock, Integer size)
    {
        if (stock == null || stock < 1)
        {
            throw BusinessException.builder()
                    .code(HttpStatus.BAD_REQUEST)
                    .message("Stock must be greater than or equal to 1")
                    .build();
        }
        if (size == null || size < 1)
        {
            throw BusinessException.builder()
                    .code(HttpStatus.BAD_REQUEST)
                    .message("Size must be a positive integer")
                    .build();
        }
    }
}
