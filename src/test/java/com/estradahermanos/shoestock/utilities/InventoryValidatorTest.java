package com.estradahermanos.shoestock.utilities;

import com.estradahermanos.shoestock.error.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InventoryValidatorTest
{
    private final InventoryValidator validator = new InventoryValidator();

    @Test
    void acceptsValidStockAndSize()
    {
        assertDoesNotThrow(() -> validator.validateRegister(1, 40));
    }

    @Test
    void rejectsStockBelowOne()
    {
        assertThrows(BusinessException.class, () -> validator.validateRegister(0, 40));
    }

    @Test
    void rejectsNullStock()
    {
        assertThrows(BusinessException.class, () -> validator.validateRegister(null, 40));
    }

    @Test
    void rejectsNonPositiveSize()
    {
        assertThrows(BusinessException.class, () -> validator.validateRegister(5, 0));
    }
}
