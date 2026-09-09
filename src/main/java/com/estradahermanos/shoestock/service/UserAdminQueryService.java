package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.response.UserAdminResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.mapper.UserAdminMapper;
import com.estradahermanos.shoestock.repository.entities.UserAdmin;
import com.estradahermanos.shoestock.repository.repositories.UserAdminRepository;
import com.estradahermanos.shoestock.utilities.ExceptionLog;
import com.estradahermanos.shoestock.utilities.UserAdminValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAdminQueryService
{
    private final UserAdminRepository userAdminRepository;
    private final UserAdminMapper     userAdminMapper;
    private final UserAdminValidator  userAdminValidator;

    /** Returns basic data of an administrative user identified by DPI. */
    public UserAdminResponseDTO findByDpi(String dpi)
    {
        log.info("Querying user admin by DPI");
        try
        {
            userAdminValidator.validateDpi(dpi);
            UserAdmin userAdmin = userAdminRepository.findByDpi(dpi)
                    .orElseThrow(() -> BusinessException.builder()
                            .code(HttpStatus.NOT_FOUND)
                            .message("User admin not found")
                            .build());
            return userAdminMapper.toResponse(userAdmin);
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to query user admin", exception);
            throw BusinessException.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Could not query user admin")
                    .build();
        }
    }

    /** Returns the basic data of all administrative users. */
    public List<UserAdminResponseDTO> findAll()
    {
        log.info("Querying all user admins");
        try
        {
            return userAdminMapper.toResponseList(userAdminRepository.findAll());
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to list user admins", exception);
            throw BusinessException.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Could not list user admins")
                    .build();
        }
    }
}
