package com.estradahermanos.shoestock.utilities;

import com.estradahermanos.shoestock.error.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void acceptsValidAmount()
    {
        assertDoesNotThrow(() -> validator.validateAmount(1));
    }

    @Test
    void rejectsAmountBelowOne()
    {
        assertThrows(BusinessException.class, () -> validator.validateAmount(0));
    }

    @Test
    void acceptsQueryByShoeCodeOnly()
    {
        assertFalse(validator.requiresExactVariant(null, null));
        assertFalse(validator.requiresExactVariant("  ", null));
    }

    @Test
    void acceptsExactVariantQuery()
    {
        assertTrue(validator.requiresExactVariant("Negro", 40));
    }

    @Test
    void rejectsColorWithoutSize()
    {
        BusinessException exception = assertThrows(
                BusinessException.class, () -> validator.requiresExactVariant("Negro", null));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
        assertEquals("Color and size must be sent together", exception.getMessage());
    }

    @Test
    void rejectsSizeWithoutColor()
    {
        assertThrows(BusinessException.class, () -> validator.requiresExactVariant(null, 40));
    }
}
