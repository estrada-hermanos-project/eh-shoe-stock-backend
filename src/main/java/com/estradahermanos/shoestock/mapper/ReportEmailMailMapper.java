package com.estradahermanos.shoestock.mapper;

import com.estradahermanos.shoestock.dto.mail.MailAttachment;
import com.estradahermanos.shoestock.dto.mail.MailMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReportEmailMailMapper
{
    @Mapping(target = "recipient", source = "recipient")
    @Mapping(target = "subject", source = "subject")
    @Mapping(target = "body", source = "body")
    @Mapping(target = "attachment", source = "attachment")
    MailMessage toMessage(String recipient, String subject, String body, MailAttachment attachment);

    @Mapping(target = "fileName", source = "fileName")
    @Mapping(target = "contentType", constant = "application/pdf")
    @Mapping(target = "content", source = "content")
    MailAttachment toAttachment(String fileName, byte[] content);
}
