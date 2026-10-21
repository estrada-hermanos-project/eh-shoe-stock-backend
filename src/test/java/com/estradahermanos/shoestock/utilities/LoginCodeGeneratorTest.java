package com.estradahermanos.shoestock.utilities;

import org.junit.jupiter.api.Test;

import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginCodeGeneratorTest
{
    @Test
    void padsTheCodeToFourDigits()
    {
        assertEquals("0007", new LoginCodeGenerator(fixed(7)).generate());
        assertEquals("0000", new LoginCodeGenerator(fixed(0)).generate());
        assertEquals("9999", new LoginCodeGenerator(fixed(9999)).generate());
    }

    @Test
    void defaultGeneratorStaysInsideFourDigits()
    {
        LoginCodeGenerator generator = new LoginCodeGenerator();
        for (int attempt = 0; attempt < 30; attempt++)
        {
            assertTrue(generator.generate().matches("[0-9]{4}"));
        }
    }

    private SecureRandom fixed(int value)
    {
        return new SecureRandom()
        {
            @Override
            public int nextInt(int bound)
            {
                assertEquals(10_000, bound);
                return value;
            }
        };
    }
}
