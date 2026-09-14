package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.request.RegisterStockRequestDTO;
import com.estradahermanos.shoestock.dto.response.ShoeStockResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.mapper.ShoeStockMapper;
import com.estradahermanos.shoestock.repository.entities.ShoeStock;
import com.estradahermanos.shoestock.repository.repositories.ShoeRepository;
import com.estradahermanos.shoestock.repository.repositories.ShoeStockRepository;
import com.estradahermanos.shoestock.utilities.ExceptionLog;
import com.estradahermanos.shoestock.utilities.InventoryValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryDecreaseService
{
    private final ShoeRepository      shoeRepository;
    private final ShoeStockRepository shoeStockRepository;
    private final ShoeStockMapper     shoeStockMapper;
    private final InventoryValidator  inventoryValidator;

    /** Descuenta stock de una variante existente. Pensado para el modulo de Ventas. */
    @Transactional
    public ShoeStockResponseDTO decrease(RegisterStockRequestDTO request)
    {
        log.info("Decreasing stock");
        try
        {
            if (request == null)
            {
                throw BusinessException.builder()
                        .code(HttpStatus.BAD_REQUEST)
                        .message("Request body is required")
                        .build();
            }

            inventoryValidator.validateRegister(request.getStock(), request.getSize());

            if (!shoeRepository.existsByCode(request.getShoeId()))
            {
                throw BusinessException.builder()
                        .code(HttpStatus.NOT_FOUND)
                        .message("Shoe not found")
                        .build();
            }

            ShoeStock variant = shoeStockRepository
                    .findByShoeIdAndColorAndSize(request.getShoeId(), request.getColor(), request.getSize())
                    .orElseThrow(() -> BusinessException.builder()
                            .code(HttpStatus.NOT_FOUND)
                            .message("Stock not found for the given shoe, color and size")
                            .build());

            if (variant.getStock() < request.getStock())
            {
                throw BusinessException.builder()
                        .code(HttpStatus.CONFLICT)
                        .message("Insufficient stock to decrease")
                        .build();
            }

            variant.setStock(variant.getStock() - request.getStock());
            ShoeStock saved = shoeStockRepository.save(variant);
            log.info("Stock decreased");
            warnIfLowStock(saved);
            return shoeStockMapper.toResponse(saved);
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to decrease stock", exception);
            throw BusinessException.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Could not decrease stock")
                    .build();
        }
    }

    /** Avisa por log cuando la variante alcanza o baja de su stock minimo. */
    private void warnIfLowStock(ShoeStock variant)
    {
        if (variant.getStock() <= variant.getMinStock())
        {
            log.warn("Low stock alert: shoe={} color={} size={} stock={} minStock={}",
                    variant.getShoeId(), variant.getColor(), variant.getSize(),
                    variant.getStock(), variant.getMinStock());
        }
    }
}
