package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.repository.repositories.ShoeRepository;
import com.estradahermanos.shoestock.utilities.ExceptionLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShoeDeleteService
{
    private final ShoeRepository shoeRepository;

    /** Elimina un estilo de zapato por su codigo. */
    @Transactional
    public void deleteByCode(String code)
    {
        log.info("Deleting shoe");
        try
        {
            if (!shoeRepository.existsByCode(code))
            {
                throw BusinessException.builder()
                        .code(HttpStatus.NOT_FOUND)
                        .message("Shoe not found")
                        .build();
            }
            shoeRepository.deleteByCode(code);
            log.info("Shoe deleted");
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to delete shoe", exception);
            throw BusinessException.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Could not delete shoe")
                    .build();
        }
    }
}
