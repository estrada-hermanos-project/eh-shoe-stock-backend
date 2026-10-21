package com.estradahermanos.shoestock.dto.mail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailMessage
{
    private String recipient;

    private String subject;

    private String body;

    private MailAttachment attachment;
}
