package com.estradahermanos.shoestock.utilities;

import com.estradahermanos.shoestock.error.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SupplierValidatorTest
{
    private final SupplierValidator validator = new SupplierValidator();

    @Test
    void acceptsValidWritableFields()
    {
        assertDoesNotThrow(() -> validator.validateWritableFields("Carlos Estrada", "55512345"));
    }

    @Test
    void rejectsBlankFullName()
    {
        assertThrows(BusinessException.class, () -> validator.validateFullName("  "));
    }

    @Test
    void rejectsPhoneWithWrongLength()
    {
        assertThrows(BusinessException.class, () -> validator.validatePhone("123"));
    }

    @Test
    void rejectsPhoneWithNonDigits()
    {
        assertThrows(BusinessException.class, () -> validator.validatePhone("5551234a"));
    }
}
