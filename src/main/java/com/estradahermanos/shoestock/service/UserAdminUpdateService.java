package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.request.UpdateUserAdminRequestDTO;
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
public class UserAdminUpdateService
{
    private final UserAdminRepository userAdminRepository;
    private final UserAdminMapper     userAdminMapper;
    private final UserAdminValidator  userAdminValidator;

    /** Updates an existing administrative user identified by DPI. */
    @Transactional
    public UserAdminResponseDTO update(String dpi, UpdateUserAdminRequestDTO request)
    {
        log.info("Updating user admin");
        try
        {
            if (request == null)
            {
                throw BusinessException.builder()
                        .code(HttpStatus.BAD_REQUEST)
                        .message("Request body is required")
                        .build();
            }

            userAdminValidator.validateDpi(dpi);
            userAdminValidator.validateWritableFields(request.getFullName(), request.getPhone(), request.getEmail());

            UserAdmin userAdmin = userAdminRepository.findByDpi(dpi)
                    .orElseThrow(() -> BusinessException.builder()
                            .code(HttpStatus.NOT_FOUND)
                            .message("User admin not found")
                            .build());

            userAdminMapper.updateEntity(request, userAdmin);
            UserAdmin saved = userAdminRepository.save(userAdmin);
            log.info("User admin updated");
            return userAdminMapper.toResponse(saved);
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to update user admin", exception);
            throw BusinessException.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Could not update user admin")
                    .build();
        }
    }
}
