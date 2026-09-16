package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.response.ShoeResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.mapper.ShoeMapper;
import com.estradahermanos.shoestock.repository.entities.Shoe;
import com.estradahermanos.shoestock.repository.entities.Supplier;
import com.estradahermanos.shoestock.repository.repositories.ShoeRepository;
import com.estradahermanos.shoestock.repository.repositories.SupplierRepository;
import com.estradahermanos.shoestock.utilities.ExceptionLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShoeQueryService
{
    private final ShoeRepository     shoeRepository;
    private final SupplierRepository supplierRepository;
    private final ShoeMapper         shoeMapper;

    /** Consulta un estilo por codigo y resuelve el nombre de su proveedor. */
    public ShoeResponseDTO findByCode(String code)
    {
        log.info("Querying shoe by code");
        try
        {
            Shoe shoe = shoeRepository.findByCode(code)
                    .orElseThrow(() -> BusinessException.builder()
                            .code(HttpStatus.NOT_FOUND)
                            .message("Shoe not found")
                            .build());
            return shoeMapper.toResponse(shoe, resolveSupplierName(shoe.getSupplier()));
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to query shoe", exception);
            throw BusinessException.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Could not query shoe")
                    .build();
        }
    }

    /** Lista todos los estilos resolviendo el nombre del proveedor con un mapa (evita N+1). */
    public List<ShoeResponseDTO> findAll()
    {
        log.info("Querying all shoes");
        try
        {
            Map<Integer, String> supplierNames = supplierRepository.findAll().stream()
                    .collect(Collectors.toMap(Supplier::getId, Supplier::getFullName));

            return shoeRepository.findAll().stream()
                    .map(shoe -> shoeMapper.toResponse(shoe, supplierNames.get(shoe.getSupplier())))
                    .toList();
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to list shoes", exception);
            throw BusinessException.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Could not list shoes")
                    .build();
        }
    }

    /** Lista los estilos de un proveedor existente. */
    public List<ShoeResponseDTO> findBySupplier(Integer supplierId)
    {
        log.info("Querying shoes by supplier");
        try
        {
            Supplier supplier = supplierRepository.findById(supplierId)
                    .orElseThrow(() -> BusinessException.builder()
                            .code(HttpStatus.NOT_FOUND)
                            .message("Supplier not found")
                            .build());

            return shoeRepository.findBySupplier(supplierId).stream()
                    .map(shoe -> shoeMapper.toResponse(shoe, supplier.getFullName()))
                    .toList();
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to list shoes by supplier", exception);
            throw BusinessException.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Could not list shoes by supplier")
                    .build();
        }
    }

    /** Resuelve el nombre del proveedor a partir de su id; null si no existe. */
    private String resolveSupplierName(Integer supplierId)
    {
        return supplierRepository.findById(supplierId)
                .map(Supplier::getFullName)
                .orElse(null);
    }
}
