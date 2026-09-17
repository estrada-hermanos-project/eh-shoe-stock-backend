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
public class InventoryRegisterService
{
    private final ShoeRepository      shoeRepository;
    private final ShoeStockRepository shoeStockRepository;
    private final ShoeStockMapper     shoeStockMapper;
    private final InventoryValidator  inventoryValidator;

    /** Ingresa mercaderia: crea la variante si no existe o incrementa su stock si ya existe. */
    @Transactional
    public ShoeStockResponseDTO register(RegisterStockRequestDTO request)
    {
        log.info("Registering incoming stock");
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
                    .map(existing ->
                    {
                        existing.setStock(existing.getStock() + request.getStock());
                        return existing;
                    })
                    .orElseGet(() -> ShoeStock.builder()
                            .shoeId(request.getShoeId())
                            .color(request.getColor())
                            .size(request.getSize())
                            .stock(request.getStock())
                            .minStock(0)
                            .build());

            ShoeStock saved = shoeStockRepository.save(variant);
            log.info("Stock registered");
            return shoeStockMapper.toResponse(saved);
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to register stock", exception);
            throw BusinessException.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Could not register stock")
                    .build();
        }
    }
}
