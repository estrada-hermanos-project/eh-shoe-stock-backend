package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.mail.MailMessage;
import com.estradahermanos.shoestock.dto.request.PasswordRecoveryRequestDTO;
import com.estradahermanos.shoestock.dto.response.MessageResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.repository.entities.PasswordRecoveryToken;
import com.estradahermanos.shoestock.repository.entities.UserAccount;
import com.estradahermanos.shoestock.repository.entities.UserAdmin;
import com.estradahermanos.shoestock.repository.repositories.PasswordRecoveryTokenRepository;
import com.estradahermanos.shoestock.repository.repositories.UserAccountRepository;
import com.estradahermanos.shoestock.repository.repositories.UserAdminRepository;
import com.estradahermanos.shoestock.mapper.PasswordRecoveryTokenMapper;
import com.estradahermanos.shoestock.utilities.BusinessDate;
import com.estradahermanos.shoestock.utilities.ExceptionLog;
import com.estradahermanos.shoestock.utilities.PasswordRecoveryTokenGenerator;
import com.estradahermanos.shoestock.utilities.UserAccountValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class PasswordRecoveryRequestService
{
    static final int TOKEN_DURATION_MINUTES = 30;
    static final String ACCEPTED_MESSAGE = "If the account exists, a recovery email was sent";

    private final UserAccountRepository userAccountRepository;
    private final UserAdminRepository userAdminRepository;
    private final PasswordRecoveryTokenRepository tokenRepository;
    private final PasswordRecoveryTokenMapper tokenMapper;
    private final PasswordRecoveryTokenGenerator tokenGenerator;
    private final MailSendService mailSendService;
    private final UserAccountValidator userAccountValidator;
    private final String frontendBaseUrl;

    public PasswordRecoveryRequestService(
            UserAccountRepository userAccountRepository,
            UserAdminRepository userAdminRepository,
            PasswordRecoveryTokenRepository tokenRepository,
            PasswordRecoveryTokenMapper tokenMapper,
            PasswordRecoveryTokenGenerator tokenGenerator,
            MailSendService mailSendService,
            UserAccountValidator userAccountValidator,
            @Value("${app.frontend.base-url}") String frontendBaseUrl)
    {
        this.userAccountRepository = userAccountRepository;
        this.userAdminRepository = userAdminRepository;
        this.tokenRepository = tokenRepository;
        this.tokenMapper = tokenMapper;
        this.tokenGenerator = tokenGenerator;
        this.mailSendService = mailSendService;
        this.userAccountValidator = userAccountValidator;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    /** Pide la recuperación. La respuesta no revela si el usuario existe ni si tiene correo. */
    @Transactional
    public MessageResponseDTO request(PasswordRecoveryRequestDTO request)
    {
        log.info("Requesting password recovery");
        try
        {
            if (Objects.isNull(request))
            {
                throw BusinessException.builder()
                        .code(HttpStatus.BAD_REQUEST)
                        .message("Request body is required")
                        .build();
            }
            userAccountValidator.validateUsername(request.getUsername());
            return sendIfPossible(request.getUsername());
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to request password recovery", exception);
            throw BusinessException.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Could not request password recovery")
                    .build();
        }
    }

    private MessageResponseDTO sendIfPossible(String username)
    {
        Optional<UserAccount> account = userAccountRepository.findByUsername(username);
        if (account.isEmpty())
        {
            log.info("Password recovery requested for an unknown account");
            return accepted();
        }
        Optional<String> email = recipientEmail(account.get());
        if (email.isEmpty())
        {
            log.warn("Password recovery skipped because the administrator has no email. username={}", username);
            return accepted();
        }
        PasswordRecoveryToken token = issueToken(username);
        mailSendService.send(recoveryMessage(email.get(), username, token.getToken()));
        log.info("Password recovery email sent");
        return accepted();
    }

    private Optional<String> recipientEmail(UserAccount account)
    {
        return userAdminRepository.findByDpi(account.getDpi())
                .map(UserAdmin::getEmail)
                .map(String::trim)
                .filter(StringUtils::hasText);
    }

    private PasswordRecoveryToken issueToken(String username)
    {
        List<PasswordRecoveryToken> openTokens = tokenRepository.findUnusedByUsername(username);
        openTokens.forEach(token -> token.setUsed(true));
        tokenRepository.saveAll(openTokens);
        LocalDateTime creationDate = LocalDateTime.now(BusinessDate.ZONE);
        PasswordRecoveryToken token = tokenMapper.toEntity(
                tokenGenerator.generate(),
                username,
                creationDate,
                creationDate.plusMinutes(TOKEN_DURATION_MINUTES));
        return tokenRepository.save(token);
    }

    private MailMessage recoveryMessage(String email, String username, String token)
    {
        String link = recoveryLink(username, token);
        String body = "Recibimos una solicitud para recuperar la contraseña de la cuenta "
                + username
                + ".\nAbra el siguiente enlace para ingresar una contraseña nueva. El enlace vence en "
                + TOKEN_DURATION_MINUTES
                + " minutos y solo puede usarse una vez.\n\n"
                + link
                + "\n\nSi usted no solicitó este cambio, ignore este correo.\n";
        return MailMessage.builder()
                .recipient(email)
                .subject("Recuperación de contraseña")
                .body(body)
                .build();
    }

    private String recoveryLink(String username, String token)
    {
        return UriComponentsBuilder.fromUriString(frontendRoot() + "/nueva-contrasena.html")
                .queryParam("username", username)
                .queryParam("token", token)
                .build()
                .encode()
                .toUriString();
    }

    private String frontendRoot()
    {
        String root = frontendBaseUrl.trim();
        if (root.endsWith("/"))
        {
            return root.substring(0, root.length() - 1);
        }
        return root;
    }

    private MessageResponseDTO accepted()
    {
        return MessageResponseDTO.builder().message(ACCEPTED_MESSAGE).build();
    }
}
