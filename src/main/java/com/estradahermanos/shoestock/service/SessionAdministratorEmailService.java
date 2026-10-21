package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.repository.entities.UserAccount;
import com.estradahermanos.shoestock.repository.entities.UserAdmin;
import com.estradahermanos.shoestock.repository.entities.UserSession;
import com.estradahermanos.shoestock.repository.repositories.UserAccountRepository;
import com.estradahermanos.shoestock.repository.repositories.UserAdminRepository;
import com.estradahermanos.shoestock.repository.repositories.UserSessionRepository;
import com.estradahermanos.shoestock.utilities.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionAdministratorEmailService
{
    static final String MISSING_EMAIL_MESSAGE = "The administrator has no email registered";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;
    private final UserSessionRepository userSessionRepository;
    private final UserAccountRepository userAccountRepository;
    private final UserAdminRepository userAdminRepository;

    /** Resuelve el correo del administrador a partir del JWT de sesión, no de un dato del cliente. */
    public String resolve(String authorization)
    {
        String token = rawToken(authorization);
        UserSession session = sessionOf(token);
        UserAccount account = accountOf(session);
        return administratorEmail(account);
    }

    private String rawToken(String authorization)
    {
        if (!StringUtils.hasText(authorization))
        {
            throw unauthorized("Session token is required");
        }
        String value = stripBearer(authorization.trim());
        if (!StringUtils.hasText(value))
        {
            throw unauthorized("Session token is required");
        }
        return value;
    }

    private String stripBearer(String value)
    {
        String prefix = BEARER_PREFIX.toLowerCase(Locale.ROOT);
        if (!value.toLowerCase(Locale.ROOT).startsWith(prefix))
        {
            return value;
        }
        return value.substring(prefix.length()).trim();
    }

    private UserSession sessionOf(String token)
    {
        String sessionCode;
        try
        {
            sessionCode = jwtProvider.sessionCodeFrom(token);
        }
        catch (RuntimeException exception)
        {
            throw unauthorized("Invalid or expired session");
        }
        UserSession session = userSessionRepository.findBySessionCode(sessionCode)
                .orElseThrow(() -> unauthorized("Invalid or expired session"));
        if (Objects.isNull(session.getExpirationDate()) || session.getExpirationDate().isBefore(LocalDateTime.now()))
        {
            throw unauthorized("Invalid or expired session");
        }
        return session;
    }

    private UserAccount accountOf(UserSession session)
    {
        return userAccountRepository.findByUsername(session.getUserAccountId())
                .orElseThrow(() -> unauthorized("Invalid or expired session"));
    }

    private String administratorEmail(UserAccount account)
    {
        return userAdminRepository.findByDpi(account.getDpi())
                .map(UserAdmin::getEmail)
                .map(String::trim)
                .filter(StringUtils::hasText)
                .orElseThrow(() -> missingEmail(account.getUsername()));
    }

    private BusinessException missingEmail(String username)
    {
        log.warn("Report email stopped because the administrator has no email. username={}", username);
        return BusinessException.builder()
                .code(HttpStatus.BAD_REQUEST)
                .message(MISSING_EMAIL_MESSAGE)
                .build();
    }

    private BusinessException unauthorized(String message)
    {
        return BusinessException.builder()
                .code(HttpStatus.UNAUTHORIZED)
                .message(message)
                .build();
    }
}
