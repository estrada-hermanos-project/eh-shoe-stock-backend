package com.estradahermanos.shoestock.mapper;

import com.estradahermanos.shoestock.dto.mail.MailMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StockAlertMailMapper
{
    @Mapping(target = "attachment", ignore = true)
    @Mapping(target = "recipient", source = "recipient")
    @Mapping(target = "subject", source = "subject")
    @Mapping(target = "body", source = "body")
    MailMessage toMessage(String recipient, String subject, String body);
}
