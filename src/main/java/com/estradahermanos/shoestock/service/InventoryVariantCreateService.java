package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.request.CreateVariantRequestDTO;
import com.estradahermanos.shoestock.dto.response.ShoeStockResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.mapper.ShoeStockMapper;
import com.estradahermanos.shoestock.repository.repositories.ShoeRepository;
import com.estradahermanos.shoestock.repository.repositories.ShoeStockRepository;
import com.estradahermanos.shoestock.utilities.ExceptionLog;
import com.estradahermanos.shoestock.utilities.InventoryValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryVariantCreateService
{
    private final ShoeRepository      shoeRepository;
    private final ShoeStockRepository shoeStockRepository;
    private final ShoeStockMapper     shoeStockMapper;
    private final InventoryValidator  inventoryValidator;

    /** Registra color y talla con stock 0. Los pares solo entran al recibir un pedido. */
    @Transactional
    public ShoeStockResponseDTO create(CreateVariantRequestDTO request)
    {
        log.info("Creating stock variant without pairs");
        try
        {
            if (Objects.isNull(request))
            {
                throw BusinessException.builder()
                        .code(HttpStatus.BAD_REQUEST)
                        .message("Request body is required")
                        .build();
            }

            inventoryValidator.validateVariant(request.getColor(), request.getSize());

            if (!shoeRepository.existsByCode(request.getShoeId()))
            {
                throw BusinessException.builder()
                        .code(HttpStatus.NOT_FOUND)
                        .message("Shoe not found")
                        .build();
            }

            if (shoeStockRepository
                    .findByShoeIdAndColorAndSize(request.getShoeId(), request.getColor(), request.getSize())
                    .isPresent())
            {
                throw BusinessException.builder()
                        .code(HttpStatus.CONFLICT)
                        .message("A variant with this color and size already exists")
                        .build();
            }

            ShoeStockResponseDTO response = shoeStockMapper.toResponse(
                    shoeStockRepository.save(shoeStockMapper.toNewVariant(request)));
            log.info("Stock variant created");
            return response;
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to create stock variant", exception);
            throw BusinessException.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Could not create variant")
                    .build();
        }
    }
}
