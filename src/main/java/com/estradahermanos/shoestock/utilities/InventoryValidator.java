package com.estradahermanos.shoestock.utilities;

import com.estradahermanos.shoestock.error.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class InventoryValidator
{
    public boolean requiresExactVariant(String color, Integer size)
    {
        boolean hasColor = !Objects.isNull(color) && !color.isBlank();
        boolean hasSize = !Objects.isNull(size);
        if (hasColor != hasSize)
        {
            throw BusinessException.builder()
                    .code(HttpStatus.BAD_REQUEST)
                    .message("Color and size must be sent together")
                    .build();
        }
        return hasColor;
    }

    public void validateAmount(Integer amount)
    {
        if (Objects.isNull(amount) || amount < 1)
        {
            throw BusinessException.builder()
                    .code(HttpStatus.BAD_REQUEST)
                    .message("Stock must be greater than or equal to 1")
                    .build();
        }
    }

    public void validateRegister(Integer stock, Integer size)
    {
        validateAmount(stock);
        if (Objects.isNull(size) || size < 1)
        {
            throw BusinessException.builder()
                    .code(HttpStatus.BAD_REQUEST)
                    .message("Size must be a positive integer")
                    .build();
        }
    }
}
