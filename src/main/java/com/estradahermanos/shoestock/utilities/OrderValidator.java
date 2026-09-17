package com.estradahermanos.shoestock.utilities;

import com.estradahermanos.shoestock.error.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class OrderValidator
{
    public void validateCreate(String orderId)
    {
        if (!StringUtils.hasText(orderId))
        {
            throw BusinessException.builder()
                    .code(HttpStatus.BAD_REQUEST)
                    .message("Order id is required")
                    .build();
        }
    }

    public void validateDetail(Integer amount)
    {
        if (amount == null || amount < 1)
        {
            throw BusinessException.builder()
                    .code(HttpStatus.BAD_REQUEST)
                    .message("Amount must be greater than or equal to 1")
                    .build();
        }
    }

    public OrderStatusEnum parseStatus(String status)
    {
        if (!OrderStatusEnum.isValid(status))
        {
            throw BusinessException.builder()
                    .code(HttpStatus.BAD_REQUEST)
                    .message("Order status must be PENDIENTE or RECIBIDA")
                    .build();
        }
        return OrderStatusEnum.valueOf(status);
    }
}
