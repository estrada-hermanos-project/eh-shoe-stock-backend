package com.estradahermanos.shoestock.mapper;

import com.estradahermanos.shoestock.repository.entities.LoginCode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface LoginCodeMapper
{
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "used", constant = "false")
    @Mapping(target = "code", source = "code")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "creationDate", source = "creationDate")
    @Mapping(target = "expirationDate", source = "expirationDate")
    LoginCode toEntity(String code, String username, LocalDateTime creationDate, LocalDateTime expirationDate);
}
