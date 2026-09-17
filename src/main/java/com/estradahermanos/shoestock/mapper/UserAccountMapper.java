package com.estradahermanos.shoestock.mapper;

import com.estradahermanos.shoestock.dto.response.UserAccountResponseDTO;
import com.estradahermanos.shoestock.repository.entities.UserAccount;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserAccountMapper
{
    UserAccountResponseDTO toResponse(UserAccount userAccount);
}
