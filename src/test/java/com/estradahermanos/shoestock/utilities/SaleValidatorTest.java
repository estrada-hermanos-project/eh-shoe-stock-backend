package com.estradahermanos.shoestock.utilities;

import com.estradahermanos.shoestock.error.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SaleValidatorTest
{
    private final SaleValidator validator = new SaleValidator();

    @Test
    void acceptsValidNameAndColor()
    {
        assertDoesNotThrow(() -> validator.validateCreate("Oxford clasico", "Negro"));
    }

    @Test
    void rejectsBlankName()
    {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> validator.validateCreate("  ", "Negro"));

        assertEquals("Name is required", exception.getMessage());
    }

    @Test
    void rejectsBlankColor()
    {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> validator.validateCreate("Oxford clasico", ""));

        assertEquals("Color is required", exception.getMessage());
    }

    @Test
    void rejectsNullName()
    {
        assertThrows(BusinessException.class, () -> validator.validateCreate(null, "Negro"));
    }
}
