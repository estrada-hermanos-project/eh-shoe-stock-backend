package com.estradahermanos.shoestock.utilities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderStatusEnumTest
{
    @Test
    void acceptsPendingAndReceived()
    {
        assertTrue(OrderStatusEnum.isValid("PENDIENTE"));
        assertTrue(OrderStatusEnum.isValid("RECIBIDA"));
    }

    @Test
    void rejectsUnknownOrBlankStatus()
    {
        assertFalse(OrderStatusEnum.isValid("CANCELADO"));
        assertFalse(OrderStatusEnum.isValid("pendiente"));
        assertFalse(OrderStatusEnum.isValid("RECIBIDO"));
        assertFalse(OrderStatusEnum.isValid(null));
    }
}
