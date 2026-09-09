package com.estradahermanos.shoestock.error;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@Builder
public class BusinessException extends RuntimeException
{
    private final HttpStatus code;

    private final String message;

    public BusinessException(HttpStatus code, String message)
    {
        super(message);
        this.code = code;
        this.message = message;
    }
}
