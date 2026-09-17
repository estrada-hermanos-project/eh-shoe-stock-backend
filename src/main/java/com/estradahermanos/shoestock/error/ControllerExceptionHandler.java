package com.estradahermanos.shoestock.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ControllerExceptionHandler
{
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorDTO> handleBusinessException(BusinessException businessException)
    {
        ErrorDTO errorDto = new ErrorDTO(businessException.getCode(), businessException.getMessage());
        return createResponseEntity(errorDto);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorDTO> handleIntegrity(DataIntegrityViolationException ex)
    {
        log.warn("Violación de integridad: {}", ex.getMostSpecificCause().getMessage());
        ErrorDTO error = new ErrorDTO(
                HttpStatus.CONFLICT,
                "Error de integridad: registro duplicado o con dependencias asociadas");
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> handleGeneric(Exception ex)
    {
        log.error("Error no controlado", ex);
        ErrorDTO error = new ErrorDTO(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorDTO> createResponseEntity(ErrorDTO errorDto)
    {
        return new ResponseEntity<>(errorDto, errorDto.code());
    }
}
