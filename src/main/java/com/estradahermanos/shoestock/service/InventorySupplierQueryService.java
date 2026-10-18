package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.response.SupplierVariantResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.mapper.ShoeStockMapper;
import com.estradahermanos.shoestock.repository.entities.Shoe;
import com.estradahermanos.shoestock.repository.repositories.ShoeRepository;
import com.estradahermanos.shoestock.repository.repositories.ShoeStockRepository;
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
public class InventorySupplierQueryService
{
    private final SupplierRepository  supplierRepository;
    private final ShoeRepository      shoeRepository;
    private final ShoeStockRepository shoeStockRepository;
    private final ShoeStockMapper     shoeStockMapper;

    /** Variantes de los estilos fabricados por un proveedor. No devuelve el inventario completo. */
    public List<SupplierVariantResponseDTO> findBySupplier(Integer supplierId)
    {
        log.info("Querying variants by supplier");
        try
        {
            if (!supplierRepository.existsById(supplierId))
            {
                throw BusinessException.builder()
                        .code(HttpStatus.NOT_FOUND)
                        .message("Supplier not found")
                        .build();
            }
            List<Shoe> shoes = shoeRepository.findBySupplier(supplierId);
            if (shoes.isEmpty())
            {
                return List.of();
            }
            Map<String, String> names = shoes.stream()
                    .collect(Collectors.toMap(Shoe::getCode, Shoe::getName));
            return shoeStockRepository.findByShoeIdIn(names.keySet()).stream()
                    .map(variant -> shoeStockMapper.toSupplierVariant(variant, names.get(variant.getShoeId())))
                    .toList();
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to query variants by supplier", exception);
            throw BusinessException.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Could not query variants")
                    .build();
        }
    }
}
