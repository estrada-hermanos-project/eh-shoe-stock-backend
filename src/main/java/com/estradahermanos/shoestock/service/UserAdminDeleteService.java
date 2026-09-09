package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.repository.repositories.UserAdminRepository;
import com.estradahermanos.shoestock.utilities.ExceptionLog;
import com.estradahermanos.shoestock.utilities.UserAdminValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAdminDeleteService
{
    private final UserAdminRepository userAdminRepository;
    private final UserAdminValidator  userAdminValidator;

    /** Deletes an administrative user identified by DPI. */
    @Transactional
    public void deleteByDpi(String dpi)
    {
        log.info("Deleting user admin");
        try
        {
            userAdminValidator.validateDpi(dpi);
            if (!userAdminRepository.existsByDpi(dpi))
            {
                throw BusinessException.builder()
                        .code(HttpStatus.NOT_FOUND)
                        .message("User admin not found")
                        .build();
            }
            userAdminRepository.deleteByDpi(dpi);
            log.info("User admin deleted");
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to delete user admin", exception);
            throw BusinessException.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Could not delete user admin")
                    .build();
        }
    }
}
