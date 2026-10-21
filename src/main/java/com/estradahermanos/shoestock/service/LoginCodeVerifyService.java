package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.request.LoginCodeVerifyRequestDTO;
import com.estradahermanos.shoestock.dto.response.LoginResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.repository.entities.LoginCode;
import com.estradahermanos.shoestock.repository.entities.UserAccount;
import com.estradahermanos.shoestock.repository.repositories.LoginCodeRepository;
import com.estradahermanos.shoestock.repository.repositories.UserAccountRepository;
import com.estradahermanos.shoestock.utilities.BusinessDate;
import com.estradahermanos.shoestock.utilities.ExceptionLog;
import com.estradahermanos.shoestock.utilities.UserAccountValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginCodeVerifyService
{
    private static final Pattern CODE_PATTERN = Pattern.compile("[0-9]{4}");

    private final UserAccountRepository userAccountRepository;
    private final LoginCodeRepository loginCodeRepository;
    private final UserAccountValidator userAccountValidator;
    private final UserLoginService userLoginService;
    private final BusinessDate businessDate;

    /** Confirma el código vigente y abre la misma sesión de 1 hora que antes abría el login. */
    @Transactional
    public LoginResponseDTO verify(LoginCodeVerifyRequestDTO request)
    {
        log.info("Verifying login code");
        try
        {
            requireRequest(request);
            userAccountValidator.validateUsername(request.getUsername());
            String code = normalizedCode(request.getCode());
            LoginCode loginCode = usableCode(request.getUsername(), code);
            UserAccount account = userAccountRepository.findByUsername(request.getUsername())
                    .orElseThrow(this::invalidCredentials);
            loginCode.setUsed(true);
            loginCodeRepository.save(loginCode);
            log.info("Login code verified, session opened");
            return userLoginService.openSession(account);
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to verify login code", exception);
            throw BusinessException.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Could not verify login code")
                    .build();
        }
    }

    private void requireRequest(LoginCodeVerifyRequestDTO request)
    {
        if (Objects.isNull(request))
        {
            throw BusinessException.builder()
                    .code(HttpStatus.BAD_REQUEST)
                    .message("Request body is required")
                    .build();
        }
    }

    private String normalizedCode(String code)
    {
        if (Objects.isNull(code) || !CODE_PATTERN.matcher(code.trim()).matches())
        {
            throw BusinessException.builder()
                    .code(HttpStatus.BAD_REQUEST)
                    .message("Login code must contain exactly 4 digits")
                    .build();
        }
        return code.trim();
    }

    private LoginCode usableCode(String username, String code)
    {
        LoginCode loginCode = loginCodeRepository.findLatestByUsernameAndCode(username, code)
                .orElseThrow(this::incorrectCode);
        if (!Objects.equals(loginCode.getUsername(), username))
        {
            throw incorrectCode();
        }
        if (loginCode.isUsed())
        {
            throw alreadyUsed();
        }
        if (!stillValid(loginCode))
        {
            throw expired();
        }
        return loginCode;
    }

    private boolean stillValid(LoginCode loginCode)
    {
        LocalDateTime expirationDate = loginCode.getExpirationDate();
        if (Objects.isNull(expirationDate))
        {
            return false;
        }
        return expirationDate.isAfter(businessDate.now());
    }

    private BusinessException incorrectCode()
    {
        return BusinessException.builder()
                .code(HttpStatus.UNAUTHORIZED)
                .message("El codigo es incorrecto")
                .build();
    }

    private BusinessException expired()
    {
        return BusinessException.builder()
                .code(HttpStatus.BAD_REQUEST)
                .message("El código ha expirado")
                .build();
    }

    private BusinessException alreadyUsed()
    {
        return BusinessException.builder()
                .code(HttpStatus.BAD_REQUEST)
                .message("The login code was already used")
                .build();
    }

    private BusinessException invalidCredentials()
    {
        return BusinessException.builder()
                .code(HttpStatus.UNAUTHORIZED)
                .message("Invalid credentials")
                .build();
    }
}
