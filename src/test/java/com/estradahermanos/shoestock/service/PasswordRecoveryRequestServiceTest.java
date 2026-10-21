package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.mail.MailMessage;
import com.estradahermanos.shoestock.dto.request.PasswordRecoveryRequestDTO;
import com.estradahermanos.shoestock.dto.response.MessageResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.mapper.PasswordRecoveryTokenMapper;
import com.estradahermanos.shoestock.repository.entities.PasswordRecoveryToken;
import com.estradahermanos.shoestock.repository.entities.UserAccount;
import com.estradahermanos.shoestock.repository.entities.UserAdmin;
import com.estradahermanos.shoestock.repository.repositories.PasswordRecoveryTokenRepository;
import com.estradahermanos.shoestock.repository.repositories.UserAccountRepository;
import com.estradahermanos.shoestock.repository.repositories.UserAdminRepository;
import com.estradahermanos.shoestock.utilities.PasswordRecoveryTokenGenerator;
import com.estradahermanos.shoestock.utilities.UserAccountValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordRecoveryRequestServiceTest
{
    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private UserAdminRepository userAdminRepository;

    @Mock
    private PasswordRecoveryTokenRepository tokenRepository;

    @Mock
    private PasswordRecoveryTokenMapper tokenMapper;

    @Mock
    private PasswordRecoveryTokenGenerator tokenGenerator;

    @Mock
    private MailSendService mailSendService;

    private PasswordRecoveryRequestService service;

    @BeforeEach
    void setUp()
    {
        service = serviceWith("http://localhost:5500");
    }

    @Test
    void sendsRecoveryLinkWithUsernameAndToken()
    {
        stubAccount("ana@gmail.com");
        PasswordRecoveryToken previous = PasswordRecoveryToken.builder()
                .token("old-token")
                .username("diego.estrada")
                .used(false)
                .build();
        when(tokenRepository.findUnusedByUsername("diego.estrada"))
                .thenReturn(new ArrayList<>(List.of(previous)));

        MessageResponseDTO response = service.request(request("diego.estrada"));

        assertEquals(PasswordRecoveryRequestService.ACCEPTED_MESSAGE, response.getMessage());
        assertTrue(previous.isUsed());
        ArgumentCaptor<LocalDateTime> creation = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> expiration = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(tokenMapper).toEntity(eq("raw-token"), eq("diego.estrada"), creation.capture(), expiration.capture());
        assertEquals(creation.getValue().plusMinutes(PasswordRecoveryRequestService.TOKEN_DURATION_MINUTES),
                expiration.getValue());

        ArgumentCaptor<MailMessage> mail = ArgumentCaptor.forClass(MailMessage.class);
        verify(mailSendService).send(mail.capture());
        assertEquals("ana@gmail.com", mail.getValue().getRecipient());
        assertEquals("Recuperación de contraseña", mail.getValue().getSubject());
        assertNull(mail.getValue().getAttachment());
        assertTrue(mail.getValue().getBody().contains(
                "http://localhost:5500/nueva-contrasena.html?username=diego.estrada&token=raw-token"));

        InOrder order = inOrder(tokenRepository);
        order.verify(tokenRepository).saveAll(any());
        order.verify(tokenRepository).save(any(PasswordRecoveryToken.class));
    }

    @Test
    void stripsTrailingSlashFromFrontendUrl()
    {
        stubAccount("ana@gmail.com");
        PasswordRecoveryRequestService slashed = serviceWith("http://localhost:5500/");

        slashed.request(request("diego.estrada"));

        ArgumentCaptor<MailMessage> mail = ArgumentCaptor.forClass(MailMessage.class);
        verify(mailSendService).send(mail.capture());
        assertTrue(mail.getValue().getBody().contains("http://localhost:5500/nueva-contrasena.html?"));
        assertTrue(!mail.getValue().getBody().contains("http://localhost:5500//"));
    }

    @Test
    void trimsAdministratorEmail()
    {
        stubAccount("  ana@gmail.com  ");

        service.request(request("diego.estrada"));

        ArgumentCaptor<MailMessage> mail = ArgumentCaptor.forClass(MailMessage.class);
        verify(mailSendService).send(mail.capture());
        assertEquals("ana@gmail.com", mail.getValue().getRecipient());
    }

    @Test
    void unknownUserGetsTheSameResponseAndNoEmail()
    {
        when(userAccountRepository.findByUsername("nadie")).thenReturn(Optional.empty());

        MessageResponseDTO response = service.request(request("nadie"));

        assertEquals(PasswordRecoveryRequestService.ACCEPTED_MESSAGE, response.getMessage());
        verify(mailSendService, never()).send(any());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void missingEmailGetsTheSameResponseAndNoEmail()
    {
        stubIdentity("   ");

        MessageResponseDTO response = service.request(request("diego.estrada"));

        assertEquals(PasswordRecoveryRequestService.ACCEPTED_MESSAGE, response.getMessage());
        verify(mailSendService, never()).send(any());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void missingAdministratorGetsTheSameResponseAndNoEmail()
    {
        when(userAccountRepository.findByUsername("diego.estrada")).thenReturn(Optional.of(account()));
        when(userAdminRepository.findByDpi("123")).thenReturn(Optional.empty());

        MessageResponseDTO response = service.request(request("diego.estrada"));

        assertEquals(PasswordRecoveryRequestService.ACCEPTED_MESSAGE, response.getMessage());
        verify(mailSendService, never()).send(any());
    }

    @Test
    void mailFailureReturnsControlledError()
    {
        stubAccount("ana@gmail.com");
        doThrow(BusinessException.builder()
                .code(HttpStatus.BAD_GATEWAY)
                .message("Could not send email")
                .build()).when(mailSendService).send(any());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.request(request("diego.estrada")));

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getCode());
        assertEquals("Could not send email", exception.getMessage());
    }

    @Test
    void rejectsMissingUsername()
    {
        BusinessException exception = assertThrows(BusinessException.class, () -> service.request(request("  ")));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
        assertEquals("Username is required", exception.getMessage());
        verify(userAccountRepository, never()).findByUsername(any());
    }

    @Test
    void rejectsMissingBody()
    {
        BusinessException exception = assertThrows(BusinessException.class, () -> service.request(null));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
        assertEquals("Request body is required", exception.getMessage());
    }

    @Test
    void unexpectedFailureReturnsControlledError()
    {
        when(userAccountRepository.findByUsername("diego.estrada")).thenThrow(new IllegalStateException("db"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.request(request("diego.estrada")));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getCode());
        assertEquals("Could not request password recovery", exception.getMessage());
    }

    private PasswordRecoveryRequestService serviceWith(String frontendBaseUrl)
    {
        return new PasswordRecoveryRequestService(
                userAccountRepository,
                userAdminRepository,
                tokenRepository,
                tokenMapper,
                tokenGenerator,
                mailSendService,
                new UserAccountValidator(),
                frontendBaseUrl);
    }

    private void stubAccount(String email)
    {
        stubIdentity(email);
        when(tokenGenerator.generate()).thenReturn("raw-token");
        when(tokenRepository.findUnusedByUsername("diego.estrada")).thenReturn(List.of());
        when(tokenRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenMapper.toEntity(any(), any(), any(), any())).thenAnswer(invocation -> PasswordRecoveryToken.builder()
                .token(invocation.getArgument(0))
                .username(invocation.getArgument(1))
                .creationDate(invocation.getArgument(2))
                .expirationDate(invocation.getArgument(3))
                .used(false)
                .build());
    }

    private void stubIdentity(String email)
    {
        when(userAccountRepository.findByUsername("diego.estrada")).thenReturn(Optional.of(account()));
        when(userAdminRepository.findByDpi("123")).thenReturn(Optional.of(UserAdmin.builder()
                .dpi("123")
                .fullName("Diego Estrada")
                .phone("55551234")
                .email(email)
                .build()));
    }

    private UserAccount account()
    {
        return UserAccount.builder().username("diego.estrada").passwordHash("hash").dpi("123").build();
    }

    private PasswordRecoveryRequestDTO request(String username)
    {
        return PasswordRecoveryRequestDTO.builder().username(username).build();
    }
}
