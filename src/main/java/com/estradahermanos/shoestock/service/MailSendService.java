package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.mail.MailAttachment;
import com.estradahermanos.shoestock.dto.mail.MailMessage;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.utilities.ExceptionLog;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Slf4j
@Service
public class MailSendService
{
    private final JavaMailSender mailSender;
    private final String fromAddress;

    public MailSendService(JavaMailSender mailSender, @Value("${spring.mail.username}") String fromAddress)
    {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    /** Envía un correo de texto. El adjunto es opcional y lo reutilizan el resto de notificaciones. */
    public void send(MailMessage message)
    {
        validate(message);
        try
        {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage, hasAttachment(message), StandardCharsets.UTF_8.name());
            helper.setFrom(fromAddress);
            helper.setTo(message.getRecipient().trim());
            helper.setSubject(message.getSubject());
            helper.setText(message.getBody(), false);
            attach(helper, message.getAttachment());
            mailSender.send(mimeMessage);
            log.info("Email sent");
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to send email", exception);
            throw BusinessException.builder()
                    .code(HttpStatus.BAD_GATEWAY)
                    .message("Could not send email")
                    .build();
        }
    }

    private void validate(MailMessage message)
    {
        if (Objects.isNull(message))
        {
            throw badRequest("Email message is required");
        }
        if (!StringUtils.hasText(message.getRecipient()))
        {
            throw badRequest("Email recipient is required");
        }
        if (!StringUtils.hasText(message.getSubject()) || !StringUtils.hasText(message.getBody()))
        {
            throw badRequest("Email subject and body are required");
        }
        if (hasAttachment(message) && !validAttachment(message.getAttachment()))
        {
            throw badRequest("Email attachment is invalid");
        }
    }

    private boolean hasAttachment(MailMessage message)
    {
        return !Objects.isNull(message.getAttachment());
    }

    private boolean validAttachment(MailAttachment attachment)
    {
        return StringUtils.hasText(attachment.getFileName())
                && StringUtils.hasText(attachment.getContentType())
                && !Objects.isNull(attachment.getContent())
                && attachment.getContent().length > 0;
    }

    private void attach(MimeMessageHelper helper, MailAttachment attachment) throws Exception
    {
        if (Objects.isNull(attachment))
        {
            return;
        }
        helper.addAttachment(
                attachment.getFileName(),
                new ByteArrayResource(attachment.getContent()),
                attachment.getContentType());
    }

    private BusinessException badRequest(String message)
    {
        return BusinessException.builder()
                .code(HttpStatus.BAD_REQUEST)
                .message(message)
                .build();
    }
}
