package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.request.CreateUserAdminRequestDTO;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAdminCreateService
{
    private final UserAdminRepository userAdminRepository;
    private final UserAdminMapper     userAdminMapper;
    private final UserAdminValidator  userAdminValidator;

    /** Registers a new administrative user after uniqueness and format validations. */
    @Transactional
    public UserAdminResponseDTO create(CreateUserAdminRequestDTO request)
    {
        log.info("Creating user admin");
        try
        {
            if (request == null)
            {
                throw BusinessException.builder()
                        .code(HttpStatus.BAD_REQUEST)
                        .message("Request body is required")
                        .build();
            }

            userAdminValidator.validateDpi(request.getDpi());
            userAdminValidator.validateWritableFields(request.getFullName(), request.getPhone(), request.getEmail());

            if (userAdminRepository.existsByDpi(request.getDpi()))
            {
                throw BusinessException.builder()
                        .code(HttpStatus.CONFLICT)
                        .message("A user with this DPI already exists")
                        .build();
            }

            UserAdmin saved = userAdminRepository.save(userAdminMapper.toEntity(request));
            log.info("User admin created");
            return userAdminMapper.toResponse(saved);
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to create user admin", exception);
            throw BusinessException.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Could not create user admin")
                    .build();
        }
    }
}
