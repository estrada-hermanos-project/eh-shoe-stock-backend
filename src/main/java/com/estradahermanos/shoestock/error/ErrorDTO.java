package com.estradahermanos.shoestock.error;

import org.springframework.http.HttpStatus;

public record ErrorDTO(HttpStatus code, String message)
{
}
