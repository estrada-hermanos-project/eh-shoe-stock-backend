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
public class MailAttachment
{
    private String fileName;

    private String contentType;

    private byte[] content;
}
