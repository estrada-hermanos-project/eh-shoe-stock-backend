package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.request.CreateShoeRequestDTO;
import com.estradahermanos.shoestock.dto.response.ShoeResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.mapper.ShoeMapper;
import com.estradahermanos.shoestock.repository.entities.Shoe;
import com.estradahermanos.shoestock.repository.entities.Supplier;
import com.estradahermanos.shoestock.repository.repositories.ShoeRepository;
import com.estradahermanos.shoestock.repository.repositories.SupplierRepository;
import com.estradahermanos.shoestock.utilities.ExceptionLog;
import com.estradahermanos.shoestock.utilities.ShoeValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShoeCreateService
{
    private final ShoeRepository     shoeRepository;
    private final SupplierRepository supplierRepository;
    private final ShoeMapper         shoeMapper;
    private final ShoeValidator      shoeValidator;

    /** Registra un nuevo estilo de zapato validando codigo unico, tipo y proveedor. */
    @Transactional
    public ShoeResponseDTO create(CreateShoeRequestDTO request)
    {
        log.info("Creating shoe");
        try
        {
            if (request == null)
            {
                throw BusinessException.builder()
                        .code(HttpStatus.BAD_REQUEST)
                        .message("Request body is required")
                        .build();
            }

            shoeValidator.validateCreate(request.getCode(), request.getType(), request.getName());

            if (shoeRepository.existsByCode(request.getCode()))
            {
                throw BusinessException.builder()
                        .code(HttpStatus.CONFLICT)
                        .message("A shoe with this code already exists")
                        .build();
            }

            Supplier supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> BusinessException.builder()
                            .code(HttpStatus.NOT_FOUND)
                            .message("Supplier not found")
                            .build());

            Shoe saved = shoeRepository.save(shoeMapper.toEntity(request));
            log.info("Shoe created");
            return shoeMapper.toResponse(saved, supplier.getFullName());
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to create shoe", exception);
            throw BusinessException.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Could not create shoe")
                    .build();
        }
    }
}
