package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.request.PasswordResetRequestDTO;
import com.estradahermanos.shoestock.dto.response.MessageResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.repository.entities.PasswordRecoveryToken;
import com.estradahermanos.shoestock.repository.entities.UserAccount;
import com.estradahermanos.shoestock.repository.repositories.PasswordRecoveryTokenRepository;
import com.estradahermanos.shoestock.repository.repositories.UserAccountRepository;
import com.estradahermanos.shoestock.utilities.BusinessDate;
import com.estradahermanos.shoestock.utilities.ExceptionLog;
import com.estradahermanos.shoestock.utilities.UserAccountValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService
{
    private final UserAccountRepository userAccountRepository;
    private final PasswordRecoveryTokenRepository tokenRepository;
    private final UserAccountValidator userAccountValidator;
    private final PasswordEncoder passwordEncoder;

    /** Cambia la contraseña si el token sigue vigente, no se usó y pertenece al usuario. */
    @Transactional
    public MessageResponseDTO reset(PasswordResetRequestDTO request)
    {
        log.info("Resetting password with recovery token");
        try
        {
            if (Objects.isNull(request))
            {
                throw BusinessException.builder()
                        .code(HttpStatus.BAD_REQUEST)
                        .message("Request body is required")
                        .build();
            }
            userAccountValidator.validatePasswordReset(
                    request.getUsername(),
                    request.getNewPassword(),
                    request.getConfirmPassword(),
                    request.getToken());
            PasswordRecoveryToken recoveryToken = usableToken(request.getUsername(), request.getToken());
            updatePassword(request.getUsername(), request.getNewPassword(), recoveryToken);
            log.info("Password updated");
            return MessageResponseDTO.builder().message("Password updated").build();
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to update password", exception);
            throw BusinessException.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Could not update password")
                    .build();
        }
    }

    private PasswordRecoveryToken usableToken(String username, String token)
    {
        return tokenRepository.findByToken(token)
                .filter(recoveryToken -> Objects.equals(recoveryToken.getUsername(), username))
                .filter(recoveryToken -> !recoveryToken.isUsed())
                .filter(this::isStillValid)
                .orElseThrow(this::invalidToken);
    }

    private boolean isStillValid(PasswordRecoveryToken recoveryToken)
    {
        LocalDateTime expirationDate = recoveryToken.getExpirationDate();
        if (Objects.isNull(expirationDate))
        {
            return false;
        }
        return expirationDate.isAfter(LocalDateTime.now(BusinessDate.ZONE));
    }

    private void updatePassword(String username, String newPassword, PasswordRecoveryToken recoveryToken)
    {
        UserAccount account = userAccountRepository.findByUsername(username)
                .orElseThrow(this::invalidToken);
        account.setPasswordHash(passwordEncoder.encode(newPassword));
        userAccountRepository.save(account);
        recoveryToken.setUsed(true);
        tokenRepository.save(recoveryToken);
    }

    private BusinessException invalidToken()
    {
        return BusinessException.builder()
                .code(HttpStatus.BAD_REQUEST)
                .message("Recovery token is invalid or expired")
                .build();
    }
}
