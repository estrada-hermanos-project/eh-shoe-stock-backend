package com.estradahermanos.shoestock.utilities;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class LoginCodeGenerator
{
    private static final int CODE_BOUND = 10_000;

    private final SecureRandom secureRandom;

    public LoginCodeGenerator()
    {
        this(new SecureRandom());
    }

    LoginCodeGenerator(SecureRandom secureRandom)
    {
        this.secureRandom = secureRandom;
    }

    /** Genera un código numérico de 4 dígitos, con ceros a la izquierda si hace falta. */
    public String generate()
    {
        return String.format("%04d", secureRandom.nextInt(CODE_BOUND));
    }
}
