package com.estradahermanos.shoestock.mapper;

import com.estradahermanos.shoestock.dto.request.CreateShoeRequestDTO;
import com.estradahermanos.shoestock.dto.response.ShoeResponseDTO;
import com.estradahermanos.shoestock.repository.entities.Shoe;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ShoeMapper
{
    @Mapping(target = "supplier", source = "supplierId")
    @Mapping(target = "createdAt", ignore = true)
    Shoe toEntity(CreateShoeRequestDTO request);

    @Mapping(target = "supplierName", source = "supplierName")
    ShoeResponseDTO toResponse(Shoe shoe, String supplierName);
}
