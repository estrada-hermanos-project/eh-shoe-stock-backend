package com.estradahermanos.shoestock.mapper;

import com.estradahermanos.shoestock.dto.request.CreateSupplierRequestDTO;
import com.estradahermanos.shoestock.dto.request.UpdateSupplierRequestDTO;
import com.estradahermanos.shoestock.dto.response.SupplierResponseDTO;
import com.estradahermanos.shoestock.repository.entities.Supplier;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SupplierMapper
{
    @Mapping(source = "fullName", target = "name")
    SupplierResponseDTO toResponse(Supplier supplier);

    List<SupplierResponseDTO> toResponseList(List<Supplier> suppliers);

    @Mapping(target = "id", ignore = true)
    Supplier toEntity(CreateSupplierRequestDTO request);

    @Mapping(target = "id", ignore = true)
    void updateEntity(UpdateSupplierRequestDTO request, @MappingTarget Supplier supplier);
}
