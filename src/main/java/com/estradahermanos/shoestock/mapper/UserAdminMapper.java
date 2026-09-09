package com.estradahermanos.shoestock.mapper;

import com.estradahermanos.shoestock.dto.request.CreateUserAdminRequestDTO;
import com.estradahermanos.shoestock.dto.request.UpdateUserAdminRequestDTO;
import com.estradahermanos.shoestock.dto.response.UserAdminResponseDTO;
import com.estradahermanos.shoestock.repository.entities.UserAdmin;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserAdminMapper
{
    @Mapping(source = "fullName", target = "name")
    UserAdminResponseDTO toResponse(UserAdmin userAdmin);

    List<UserAdminResponseDTO> toResponseList(List<UserAdmin> userAdmins);

    @Mapping(target = "email", expression = "java(blankToNull(request.getEmail()))")
    UserAdmin toEntity(CreateUserAdminRequestDTO request);

    @Mapping(target = "dpi", ignore = true)
    @Mapping(target = "email", expression = "java(blankToNull(request.getEmail()))")
    void updateEntity(UpdateUserAdminRequestDTO request, @MappingTarget UserAdmin userAdmin);

    default String blankToNull(String value)
    {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
