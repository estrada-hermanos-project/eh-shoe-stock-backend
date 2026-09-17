package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.request.UpdateSupplierRequestDTO;
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
public class SupplierUpdateService
{
    private final SupplierRepository supplierRepository;
    private final SupplierMapper     supplierMapper;
    private final SupplierValidator  supplierValidator;

    /** Updates an existing supplier identified by id. */
    @Transactional
    public SupplierResponseDTO update(Integer id, UpdateSupplierRequestDTO request)
    {
        log.info("Updating supplier");
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

            Supplier supplier = supplierRepository.findById(id)
                    .orElseThrow(() -> BusinessException.builder()
                            .code(HttpStatus.NOT_FOUND)
                            .message("Supplier not found")
                            .build());

            if (supplierRepository.existsByPhoneAndIdNot(request.getPhone(), id))
            {
                throw BusinessException.builder()
                        .code(HttpStatus.CONFLICT)
                        .message("A supplier with this phone already exists")
                        .build();
            }

            supplierMapper.updateEntity(request, supplier);
            Supplier saved = supplierRepository.save(supplier);
            log.info("Supplier updated");
            return supplierMapper.toResponse(saved);
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to update supplier", exception);
            throw BusinessException.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Could not update supplier")
                    .build();
        }
    }
}
