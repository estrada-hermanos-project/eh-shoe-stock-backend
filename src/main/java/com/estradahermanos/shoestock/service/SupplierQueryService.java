package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.response.SupplierResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.mapper.SupplierMapper;
import com.estradahermanos.shoestock.repository.entities.Supplier;
import com.estradahermanos.shoestock.repository.repositories.SupplierRepository;
import com.estradahermanos.shoestock.utilities.ExceptionLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupplierQueryService
{
    private final SupplierRepository supplierRepository;
    private final SupplierMapper     supplierMapper;

    /** Returns the basic data of a supplier identified by id. */
    public SupplierResponseDTO findById(Integer id)
    {
        log.info("Querying supplier by id");
        try
        {
            Supplier supplier = supplierRepository.findById(id)
                    .orElseThrow(() -> BusinessException.builder()
                            .code(HttpStatus.NOT_FOUND)
                            .message("Supplier not found")
                            .build());
            return supplierMapper.toResponse(supplier);
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to query supplier", exception);
            throw BusinessException.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Could not query supplier")
                    .build();
        }
    }

    /** Returns the basic data of all suppliers. */
    public List<SupplierResponseDTO> findAll()
    {
        log.info("Querying all suppliers");
        try
        {
            return supplierMapper.toResponseList(supplierRepository.findAll());
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to list suppliers", exception);
            throw BusinessException.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Could not list suppliers")
                    .build();
        }
    }
}
