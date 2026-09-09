package com.estradahermanos.shoestock.utilities;

import com.estradahermanos.shoestock.error.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserAdminValidatorTest
{
    private UserAdminValidator validator;

    @BeforeEach
    void setUp()
    {
        validator = new UserAdminValidator();
    }

    @Test
    void validateDpiAcceptsThirteenDigits()
    {
        assertDoesNotThrow(() -> validator.validateDpi("1234567890123"));
    }

    @Test
    void validateDpiRejectsWrongLength()
    {
        BusinessException exception = assertThrows(BusinessException.class, () -> validator.validateDpi("123456789012"));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
    }

    @Test
    void validatePhoneAcceptsEightDigits()
    {
        assertDoesNotThrow(() -> validator.validatePhone("12345678"));
    }

    @Test
    void validatePhoneRejectsNonDigits()
    {
        assertThrows(BusinessException.class, () -> validator.validatePhone("1234-567"));
    }

    @Test
    void validateEmailIgnoresBlank()
    {
        assertDoesNotThrow(() -> validator.validateEmail(null));
        assertDoesNotThrow(() -> validator.validateEmail("  "));
    }

    @Test
    void validateEmailAcceptsGmail()
    {
        assertDoesNotThrow(() -> validator.validateEmail("user.name@gmail.com"));
    }

    @Test
    void validateEmailRejectsOtherDomains()
    {
        BusinessException exception = assertThrows(BusinessException.class, () -> validator.validateEmail("user@hotmail.com"));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
    }

    @Test
    void validateFullNameRejectsBlank()
    {
        assertThrows(BusinessException.class, () -> validator.validateFullName(""));
    }
}
