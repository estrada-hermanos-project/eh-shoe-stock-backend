package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.mail.MailAttachment;
import com.estradahermanos.shoestock.dto.mail.MailMessage;
import com.estradahermanos.shoestock.error.BusinessException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailSendServiceTest
{
    @Mock
    private JavaMailSender mailSender;

    private MailSendService service;
    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp()
    {
        service = new MailSendService(mailSender, "estradahermanoscfo@gmail.com");
        mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
    }

    @Test
    void sendsPlainTextEmail() throws Exception
    {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        service.send(MailMessage.builder()
                .recipient("  ana@gmail.com  ")
                .subject("Aviso")
                .body("Cuerpo")
                .build());

        verify(mailSender).send(mimeMessage);
        assertEquals("ana@gmail.com", mimeMessage.getAllRecipients()[0].toString());
        assertEquals("Aviso", mimeMessage.getSubject());
        assertTrue(mimeMessage.getContentType().contains("text/plain"));
    }

    @Test
    void sendsEmailWithAttachment() throws Exception
    {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        service.send(MailMessage.builder()
                .recipient("ana@gmail.com")
                .subject("Reporte")
                .body("Adjunto")
                .attachment(MailAttachment.builder()
                        .fileName("reporte.pdf")
                        .contentType("application/pdf")
                        .content(new byte[] {1, 2, 3})
                        .build())
                .build());

        verify(mailSender).send(mimeMessage);
        ByteArrayOutputStream rawMessage = new ByteArrayOutputStream();
        mimeMessage.writeTo(rawMessage);
        String raw = rawMessage.toString(StandardCharsets.UTF_8);
        assertTrue(raw.contains("reporte.pdf"));
        assertTrue(raw.contains("application/pdf"));
    }

    @Test
    void rejectsMissingMessage()
    {
        BusinessException exception = assertThrows(BusinessException.class, () -> service.send(null));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
        assertEquals("Email message is required", exception.getMessage());
    }

    @Test
    void rejectsMissingRecipient()
    {
        BusinessException exception = assertThrows(BusinessException.class, () -> service.send(MailMessage.builder()
                .subject("Aviso")
                .body("Cuerpo")
                .build()));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
        assertEquals("Email recipient is required", exception.getMessage());
    }

    @Test
    void rejectsMissingSubjectOrBody()
    {
        BusinessException missingSubject = assertThrows(BusinessException.class, () -> service.send(MailMessage.builder()
                .recipient("ana@gmail.com")
                .body("Cuerpo")
                .build()));
        BusinessException missingBody = assertThrows(BusinessException.class, () -> service.send(MailMessage.builder()
                .recipient("ana@gmail.com")
                .subject("Aviso")
                .build()));

        assertEquals("Email subject and body are required", missingSubject.getMessage());
        assertEquals("Email subject and body are required", missingBody.getMessage());
    }

    @Test
    void rejectsInvalidAttachment()
    {
        BusinessException exception = assertThrows(BusinessException.class, () -> service.send(MailMessage.builder()
                .recipient("ana@gmail.com")
                .subject("Aviso")
                .body("Cuerpo")
                .attachment(MailAttachment.builder()
                        .fileName(" ")
                        .contentType("application/pdf")
                        .content(new byte[] {1})
                        .build())
                .build()));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
        assertEquals("Email attachment is invalid", exception.getMessage());
    }

    @Test
    void rejectsEmptyAttachmentContent()
    {
        BusinessException exception = assertThrows(BusinessException.class, () -> service.send(MailMessage.builder()
                .recipient("ana@gmail.com")
                .subject("Aviso")
                .body("Cuerpo")
                .attachment(MailAttachment.builder()
                        .fileName("reporte.pdf")
                        .contentType("application/pdf")
                        .content(new byte[0])
                        .build())
                .build()));

        assertEquals("Email attachment is invalid", exception.getMessage());
    }

    @Test
    void mapsMailServerFailureToControlledError()
    {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailSendException("smtp down")).when(mailSender).send(mimeMessage);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.send(MailMessage.builder()
                .recipient("ana@gmail.com")
                .subject("Aviso")
                .body("Cuerpo")
                .build()));

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getCode());
        assertEquals("Could not send email", exception.getMessage());
    }
}
