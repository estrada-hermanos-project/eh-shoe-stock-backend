package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.request.CreateUserAccountRequestDTO;
import com.estradahermanos.shoestock.dto.response.UserAccountResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.mapper.UserAccountMapper;
import com.estradahermanos.shoestock.repository.entities.UserAccount;
import com.estradahermanos.shoestock.repository.repositories.UserAccountRepository;
import com.estradahermanos.shoestock.repository.repositories.UserAdminRepository;
import com.estradahermanos.shoestock.utilities.ExceptionLog;
import com.estradahermanos.shoestock.utilities.UserAccountValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAccountCreateService
{
    private final UserAccountRepository userAccountRepository;
    private final UserAdminRepository   userAdminRepository;
    private final UserAccountMapper     userAccountMapper;
    private final UserAccountValidator  userAccountValidator;
    private final PasswordEncoder       passwordEncoder;

    /** Crea la cuenta de acceso de un administrador existente, con la contrasena hasheada. */
    @Transactional
    public UserAccountResponseDTO create(CreateUserAccountRequestDTO request)
    {
        log.info("Creating user account");
        try
        {
            if (request == null)
            {
                throw BusinessException.builder()
                        .code(HttpStatus.BAD_REQUEST)
                        .message("Request body is required")
                        .build();
            }

            userAccountValidator.validateAccountCreation(request.getUsername(), request.getPassword(), request.getDpi());

            if (userAccountRepository.existsByUsername(request.getUsername()))
            {
                throw BusinessException.builder()
                        .code(HttpStatus.CONFLICT)
                        .message("An account with this username already exists")
                        .build();
            }

            if (!userAdminRepository.existsByDpi(request.getDpi()))
            {
                throw BusinessException.builder()
                        .code(HttpStatus.NOT_FOUND)
                        .message("No administrative user exists with this DPI")
                        .build();
            }

            UserAccount account = UserAccount.builder()
                    .username(request.getUsername())
                    .passwordHash(passwordEncoder.encode(request.getPassword()))
                    .dpi(request.getDpi())
                    .build();

            UserAccount saved = userAccountRepository.save(account);
            log.info("User account created");
            return userAccountMapper.toResponse(saved);
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to create user account", exception);
            throw BusinessException.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Could not create user account")
                    .build();
        }
    }
}
