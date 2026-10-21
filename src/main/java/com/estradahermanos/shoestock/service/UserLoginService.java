package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.mail.MailMessage;
import com.estradahermanos.shoestock.dto.request.LoginRequestDTO;
import com.estradahermanos.shoestock.dto.response.LoginChallengeResponseDTO;
import com.estradahermanos.shoestock.dto.response.LoginResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.mapper.LoginCodeMapper;
import com.estradahermanos.shoestock.repository.entities.LoginCode;
import com.estradahermanos.shoestock.repository.entities.UserAccount;
import com.estradahermanos.shoestock.repository.entities.UserAdmin;
import com.estradahermanos.shoestock.repository.entities.UserSession;
import com.estradahermanos.shoestock.repository.repositories.LoginCodeRepository;
import com.estradahermanos.shoestock.repository.repositories.UserAccountRepository;
import com.estradahermanos.shoestock.repository.repositories.UserAdminRepository;
import com.estradahermanos.shoestock.repository.repositories.UserSessionRepository;
import com.estradahermanos.shoestock.utilities.BusinessDate;
import com.estradahermanos.shoestock.utilities.ExceptionLog;
import com.estradahermanos.shoestock.utilities.JwtProvider;
import com.estradahermanos.shoestock.utilities.LoginCodeGenerator;
import com.estradahermanos.shoestock.utilities.SessionCodeGenerator;
import com.estradahermanos.shoestock.utilities.UserAccountValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserLoginService
{
    private static final int SESSION_DURATION_HOURS = 1;
    private static final int CODE_DURATION_MINUTES = 2;
    private static final String SECOND_STEP_MESSAGE =
            "A verification code was sent to the administrator email";
    private static final String MISSING_EMAIL_MESSAGE = "The administrator has no email registered";

    private final UserAccountRepository userAccountRepository;
    private final UserSessionRepository userSessionRepository;
    private final UserAdminRepository userAdminRepository;
    private final LoginCodeRepository loginCodeRepository;
    private final LoginCodeMapper loginCodeMapper;
    private final UserAccountValidator userAccountValidator;
    private final PasswordEncoder passwordEncoder;
    private final SessionCodeGenerator sessionCodeGenerator;
    private final JwtProvider jwtProvider;
    private final LoginCodeGenerator loginCodeGenerator;
    private final MailSendService mailSendService;
    private final BusinessDate businessDate;

    /** Valida las credenciales, envía el código de 4 dígitos y no abre la sesión. */
    @Transactional
    public LoginChallengeResponseDTO login(LoginRequestDTO request)
    {
        log.info("Authenticating user");
        try
        {
            requireRequest(request);
            userAccountValidator.validateCredentials(request.getUsername(), request.getPassword());
            UserAccount account = authenticatedAccount(request);
            String email = administratorEmail(account);
            LoginCode loginCode = issueCode(account.getUsername());
            mailSendService.send(loginCodeMessage(email, loginCode.getCode()));
            log.info("Login code sent");
            return LoginChallengeResponseDTO.builder()
                    .secondStepRequired(true)
                    .message(SECOND_STEP_MESSAGE)
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

    /** Abre la sesión de 1 hora y devuelve el JWT. Lo reutiliza la verificación del código. */
    @Transactional
    public LoginResponseDTO openSession(UserAccount account)
    {
        LocalDateTime creationDate = LocalDateTime.now();
        UserSession session = UserSession.builder()
                .sessionCode(sessionCodeGenerator.generate())
                .userAccountId(account.getUsername())
                .creationDate(creationDate)
                .expirationDate(creationDate.plusHours(SESSION_DURATION_HOURS))
                .build();
        userSessionRepository.save(session);
        log.info("Session opened");
        return LoginResponseDTO.builder()
                .token(jwtProvider.generateForSession(session.getSessionCode()))
                .build();
    }

    private void requireRequest(LoginRequestDTO request)
    {
        if (Objects.isNull(request))
        {
            throw BusinessException.builder()
                    .code(HttpStatus.BAD_REQUEST)
                    .message("Request body is required")
                    .build();
        }
    }

    private UserAccount authenticatedAccount(LoginRequestDTO request)
    {
        UserAccount account = userAccountRepository.findByUsername(request.getUsername())
                .orElseThrow(this::invalidCredentials);
        if (!passwordEncoder.matches(request.getPassword(), account.getPasswordHash()))
        {
            throw invalidCredentials();
        }
        return account;
    }

    private String administratorEmail(UserAccount account)
    {
        return userAdminRepository.findByDpi(account.getDpi())
                .map(UserAdmin::getEmail)
                .map(String::trim)
                .filter(StringUtils::hasText)
                .orElseThrow(() -> missingEmail(account.getUsername()));
    }

    private LoginCode issueCode(String username)
    {
        List<LoginCode> openCodes = loginCodeRepository.findUnusedByUsername(username);
        openCodes.forEach(loginCode -> loginCode.setUsed(true));
        loginCodeRepository.saveAll(openCodes);
        LocalDateTime creationDate = businessDate.now();
        LoginCode loginCode = loginCodeMapper.toEntity(
                loginCodeGenerator.generate(),
                username,
                creationDate,
                creationDate.plusMinutes(CODE_DURATION_MINUTES));
        return loginCodeRepository.save(loginCode);
    }

    private MailMessage loginCodeMessage(String email, String code)
    {
        String body = "Su código de inicio de sesión es "
                + code
                + ".\nVence en "
                + CODE_DURATION_MINUTES
                + " minutos y solo puede usarse una vez.\n"
                + "Si usted no intentó entrar al panel, ignore este correo.\n";
        return MailMessage.builder()
                .recipient(email)
                .subject("Código de inicio de sesión")
                .body(body)
                .build();
    }

    private BusinessException invalidCredentials()
    {
        return BusinessException.builder()
                .code(HttpStatus.UNAUTHORIZED)
                .message("Invalid credentials")
                .build();
    }

    private BusinessException missingEmail(String username)
    {
        log.warn("Login stopped because the administrator has no email. username={}", username);
        return BusinessException.builder()
                .code(HttpStatus.BAD_REQUEST)
                .message(MISSING_EMAIL_MESSAGE)
                .build();
    }
}
