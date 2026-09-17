package com.estradahermanos.shoestock.utilities;

import com.estradahermanos.shoestock.error.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShoeValidatorTest
{
    private final ShoeValidator validator = new ShoeValidator();

    @Test
    void acceptsValidShoe()
    {
        assertDoesNotThrow(() -> validator.validateCreate("OXF-001", "Caballero", "Oxford"));
    }

    @Test
    void rejectsBlankCode()
    {
        assertThrows(BusinessException.class, () -> validator.validateCreate("  ", "Dama", "Sandalia"));
    }

    @Test
    void rejectsBlankName()
    {
        assertThrows(BusinessException.class, () -> validator.validateCreate("OXF-001", "Dama", ""));
    }

    @Test
    void rejectsInvalidType()
    {
        assertThrows(BusinessException.class, () -> validator.validateCreate("OXF-001", "Unisex", "Oxford"));
    }

    @Test
    void acceptsAccentedTypes()
    {
        assertDoesNotThrow(() -> validator.validateCreate("NIN-001", "Niño", "Tenis"));
        assertDoesNotThrow(() -> validator.validateCreate("NIN-002", "Niña", "Balerina"));
    }
}
