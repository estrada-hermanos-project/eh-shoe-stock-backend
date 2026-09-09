package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.request.CreateSupplierRequestDTO;
import com.estradahermanos.shoestock.dto.response.SupplierResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.mapper.SupplierMapper;
import com.estradahermanos.shoestock.repository.entities.Supplier;
import com.estradahermanos.shoestock.repository.repositories.SupplierRepository;
import com.estradahermanos.shoestock.utilities.ExceptionLog;
import com.estradahermanos.shoestock.utilities.SupplierValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierCreateService
{
    private final SupplierRepository supplierRepository;
    private final SupplierMapper     supplierMapper;
    private final SupplierValidator  supplierValidator;

    /** Registers a new supplier after format and phone uniqueness validations. */
    @Transactional
    public SupplierResponseDTO create(CreateSupplierRequestDTO request)
    {
        log.info("Creating supplier");
        try
        {
            if (request == null)
            {
                throw BusinessException.builder()
                        .code(HttpStatus.BAD_REQUEST)
                        .message("Request body is required")
                        .build();
            }

            supplierValidator.validateWritableFields(request.getFullName(), request.getPhone());

            if (supplierRepository.existsByPhone(request.getPhone()))
            {
                throw BusinessException.builder()
                        .code(HttpStatus.CONFLICT)
                        .message("A supplier with this phone already exists")
                        .build();
            }

            Supplier saved = supplierRepository.save(supplierMapper.toEntity(request));
            log.info("Supplier created");
            return supplierMapper.toResponse(saved);
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to create supplier", exception);
            throw BusinessException.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Could not create supplier")
                    .build();
        }
    }
}
