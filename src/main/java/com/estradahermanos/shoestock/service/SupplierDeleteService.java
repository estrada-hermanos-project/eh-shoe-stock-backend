package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.repository.repositories.SupplierRepository;
import com.estradahermanos.shoestock.utilities.ExceptionLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierDeleteService
{
    private final SupplierRepository supplierRepository;

    /** Deletes a supplier identified by id. */
    @Transactional
    public void deleteById(Integer id)
    {
        log.info("Deleting supplier");
        try
        {
            if (!supplierRepository.existsById(id))
            {
                throw BusinessException.builder()
                        .code(HttpStatus.NOT_FOUND)
                        .message("Supplier not found")
                        .build();
            }
            supplierRepository.deleteById(id);
            log.info("Supplier deleted");
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to delete supplier", exception);
            throw BusinessException.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Could not delete supplier")
                    .build();
        }
    }
}
