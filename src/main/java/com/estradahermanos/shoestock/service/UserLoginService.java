package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.request.LoginRequestDTO;
import com.estradahermanos.shoestock.dto.response.LoginResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.repository.entities.UserAccount;
import com.estradahermanos.shoestock.repository.entities.UserSession;
import com.estradahermanos.shoestock.repository.repositories.UserAccountRepository;
import com.estradahermanos.shoestock.repository.repositories.UserSessionRepository;
import com.estradahermanos.shoestock.utilities.ExceptionLog;
import com.estradahermanos.shoestock.utilities.JwtProvider;
import com.estradahermanos.shoestock.utilities.SessionCodeGenerator;
import com.estradahermanos.shoestock.utilities.UserAccountValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserLoginService
{
    private static final int SESSION_DURATION_HOURS = 1;

    private final UserAccountRepository userAccountRepository;
    private final UserSessionRepository userSessionRepository;
    private final UserAccountValidator  userAccountValidator;
    private final PasswordEncoder       passwordEncoder;
    private final SessionCodeGenerator  sessionCodeGenerator;
    private final JwtProvider           jwtProvider;

    /** Autentica al administrador, abre una sesion de 1 hora y devuelve el codigo de sesion en un JWT. */
    @Transactional
    public LoginResponseDTO login(LoginRequestDTO request)
    {
        log.info("Authenticating user");
        try
        {
            if (request == null)
            {
                throw BusinessException.builder()
                        .code(HttpStatus.BAD_REQUEST)
                        .message("Request body is required")
                        .build();
            }

            userAccountValidator.validateCredentials(request.getUsername(), request.getPassword());

            UserAccount account = userAccountRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> BusinessException.builder()
                            .code(HttpStatus.UNAUTHORIZED)
                            .message("Invalid credentials")
                            .build());

            if (!passwordEncoder.matches(request.getPassword(), account.getPasswordHash()))
            {
                throw BusinessException.builder()
                        .code(HttpStatus.UNAUTHORIZED)
                        .message("Invalid credentials")
                        .build();
            }

            UserSession session = openSession(account);
            userSessionRepository.save(session);
            log.info("User authenticated, session opened");
            return LoginResponseDTO.builder()
                    .token(jwtProvider.generateForSession(session.getSessionCode()))
                    .build();
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to authenticate user", exception);
            throw BusinessException.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Could not authenticate user")
                    .build();
        }
    }

    /** Construye una sesion de 1 hora ligada a la cuenta autenticada. */
    private UserSession openSession(UserAccount account)
    {
        LocalDateTime creationDate = LocalDateTime.now();
        return UserSession.builder()
                .sessionCode(sessionCodeGenerator.generate())
                .userAccountId(account.getUsername())
                .creationDate(creationDate)
                .expirationDate(creationDate.plusHours(SESSION_DURATION_HOURS))
                .build();
    }
}
