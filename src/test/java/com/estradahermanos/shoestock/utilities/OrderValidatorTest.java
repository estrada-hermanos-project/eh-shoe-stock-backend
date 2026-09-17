package com.estradahermanos.shoestock.utilities;

import com.estradahermanos.shoestock.error.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderValidatorTest
{
    private final OrderValidator validator = new OrderValidator();

    @Test
    void acceptsValidOrderId()
    {
        assertDoesNotThrow(() -> validator.validateCreate("ORDEN-101"));
    }

    @Test
    void rejectsBlankOrderId()
    {
        BusinessException exception = assertThrows(BusinessException.class, () -> validator.validateCreate("  "));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
        assertEquals("Order id is required", exception.getMessage());
    }

    @Test
    void acceptsValidAmount()
    {
        assertDoesNotThrow(() -> validator.validateDetail(1));
    }

    @Test
    void rejectsAmountBelowOne()
    {
        BusinessException exception = assertThrows(BusinessException.class, () -> validator.validateDetail(0));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
        assertEquals("Amount must be greater than or equal to 1", exception.getMessage());
    }

    @Test
    void parseStatusAcceptsPendingAndReceived()
    {
        assertEquals(OrderStatusEnum.PENDIENTE, validator.parseStatus("PENDIENTE"));
        assertEquals(OrderStatusEnum.RECIBIDA, validator.parseStatus("RECIBIDA"));
    }

    @Test
    void parseStatusRejectsInvalidValue()
    {
        BusinessException exception = assertThrows(BusinessException.class, () -> validator.parseStatus("CANCELADO"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
        assertEquals("Order status must be PENDIENTE or RECIBIDA", exception.getMessage());
    }
}
