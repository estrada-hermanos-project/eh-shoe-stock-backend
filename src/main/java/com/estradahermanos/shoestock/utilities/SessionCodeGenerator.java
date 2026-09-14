package com.estradahermanos.shoestock.utilities;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class SessionCodeGenerator
{
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int    CODE_LENGTH = 5;

    private final SecureRandom secureRandom = new SecureRandom();

    /** Genera un codigo de sesion aleatorio de 5 caracteres. */
    public String generate()
    {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++)
        {
            code.append(ALPHABET.charAt(secureRandom.nextInt(ALPHABET.length())));
        }
        return code.toString();
    }
}
