package com.estradahermanos.shoestock.utilities;

import com.estradahermanos.shoestock.dto.response.ResponseSuccessDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public final class ResponseBuilder
{
    private ResponseBuilder()
    {
    }

    public static ResponseEntity<ResponseSuccessDTO> success(HttpStatus status, Object data)
    {
        ResponseSuccessDTO body = ResponseSuccessDTO.builder()
                .code(status)
                .data(data)
                .build();
        return new ResponseEntity<>(body, status);
    }
}
