package com.estradahermanos.shoestock.mapper;

import com.estradahermanos.shoestock.repository.entities.PasswordRecoveryToken;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface PasswordRecoveryTokenMapper
{
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "used", constant = "false")
    @Mapping(target = "token", source = "token")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "creationDate", source = "creationDate")
    @Mapping(target = "expirationDate", source = "expirationDate")
    PasswordRecoveryToken toEntity(String token, String username, LocalDateTime creationDate, LocalDateTime expirationDate);
}
